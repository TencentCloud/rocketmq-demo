# 腾讯云 RocketMQ 使用示例

本项目主要帮助用户在使用时候的代码参考，项目以版本分为不同的目录。

```
rocketmq-demo/
├── v4/          # 购买了 RocketMQ 4.x 实例的用户
│   ├── java/
│   ├── golang/
│   ├── cpp/     # 待完善
│   ├── python/
│   └── csharp/
├── v5/          # 购买了 RocketMQ 5.x 实例的用户
│   ├── java/
│   ├── golang/
│   ├── cpp/     # 待完善
│   ├── python/
│   ├── nodejs/
│   ├── rust/    # grpc-lite/ 为 Lite Topic 示例（依赖未发布 SDK）
│   └── csharp/
├── v5-dashboard/  # 腾讯云 RocketMQ Dashboard（可视化管理控制台）
└── mqtt/        # MQTT 协议示例
    └── java/
```

## 如何选择 SDK 协议

进入对应版本目录后，每种语言下会有两个子目录，对应两种连接协议：

| 目录 | 协议 | 说明 |
|------|------|------|
| `grpc/` | gRPC | RocketMQ 5.x 原生协议，推荐使用，功能最完整 |
| `remoting/` | Remoting | 兼容协议，使用 4.x SDK 连接，迁移成本低 |

> **注意：** `v4/` 下只有 `remoting/` 协议。`v5/` 下两种协议均可使用，建议优先选择 `grpc/`。

---

# RocketMQ Dashboard

[`v5-dashboard/`](v5-dashboard/README.md) 是腾讯云 RocketMQ 5.x 的可视化管理控制台，基于 Vue 3 + Spring Boot 3 构建，通过腾讯云 SDK 对接 RocketMQ 服务，支持集群、Topic、消费组、消息、角色的查看与管理。详见 [使用文档](v5-dashboard/README.md)。

---

# 5.x 已完成 demo

## gRPC 协议

| 特性    | Java 原生 | SpringBoot | CPP | Go | Rust | Python | Node.js | C# |
|:------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 普通消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 重试消息  | ✅ | ✅ | ✅ | ✅ | ✅² | ✅ | ✅ | ✅ |
| 有序消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 事务消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 定时/延迟消息 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 广播消息  | ➖ | ➖ | ➖ | ➖ | ➖ | ➖ | ➖ | ➖ |
| Tag 消息 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Lite Topic | ✅ | ➖ | ➖ | ✅ | ✅¹ | ✅ | ✅ | ✅ |

> ➖ 表示对应 SDK 不支持。
>
> - **广播消息：** 5.x gRPC SDK 没有广播消费模式，需要广播效果时可让每个消费者使用不同的消费组；如需原生广播模式请使用 `remoting/` 示例。
> - **重试消息：** Push 消费返回 FAILURE 即触发重试，最大重试次数由控制台消费组的重试策略决定，超过后进入死信。
> - **² Rust：** crates.io `rocketmq` 5.0.0 没有 PushConsumer，重试用 SimpleConsumer 不 ack、等不可见时间到期后重投来演示。
> - **SpringBoot：** gRPC 示例在 `v5/java/grpc/springboot5-grpc/`，基于 `rocketmq-v5-client-spring-boot-starter` 2.3.6；该 starter 内置的 `rocketmq-client-java` 5.0.7 没有 Lite Topic API。

### Lite Topic 示例

Lite Topic 需要在控制台创建「轻量消息」类型的一级 Topic，以及「Lite Topic 消费」模式并绑定该 Topic 的消费组。
生产者发送时通过 `liteTopic` 指定二级 Topic（无需提前创建），消费者通过 `subscribeLite` 动态订阅。

| 语言 | 示例目录 | SDK 最低版本 | 包含示例 |
|:------|:------|:------|:------|
| Java | `v5/java/grpc/rocketmqclient/.../rocketmq5/lite/` | `rocketmq-client-java` 5.2.2 | Producer、LitePushConsumer、LiteSimpleConsumer |
| Go | `v5/golang/grpc/producer/lite/`、`v5/golang/grpc/consumer/lite_push_consumer/` | `rocketmq-clients/golang/v5` v5.1.4 | Producer、LitePushConsumer |
| Python | `v5/python/grpc/lite/` | `rocketmq-python-client` 5.1.2 | Producer、LitePushConsumer、LiteSimpleConsumer |
| Node.js | `v5/nodejs/grpc/lite/` | `rocketmq-client-nodejs` 1.1.0 | Producer、LitePushConsumer、LiteSimpleConsumer |
| C# | `v5/csharp/grpc/rocketmq5_csharp_demo/` | `RocketMQ.Client` 5.2.1 | Producer、LitePushConsumer |
| Rust¹ | `v5/rust/grpc-lite/` | 未发布，git 依赖 `apache/rocketmq-clients@a3d1de8b` | Producer、LitePushConsumer |

> **¹ Rust 说明：** crates.io 上已发布的 `rocketmq` 5.0.0 不支持 Lite Topic，`v5/rust/grpc-lite/` 通过 git 地址依赖
> rocketmq-clients master 分支的固定 commit，属于未发布代码，接口可能变化，生产使用前请自行评估。详见该目录 README。
>
> **CPP 说明：** CPP SDK（包括 master 分支）暂未实现 Lite Topic。

## Remoting 协议（兼容 4.x SDK）

| 特性    | Java 原生 | SpringBoot | CPP | Go | Python | C# |
|:------|:---:|:---:|:---:|:---:|:---:|:---:|
| 普通消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 重试消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 有序消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 事务消息  | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| 广播消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Tag 消息 | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ |

---

# 4.x 已完成 demo

| 特性    | Java 原生 | SpringBoot | Spring Cloud Stream | CPP | Go | Python | C# |
|:------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 普通消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 重试消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 有序消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 事务消息  | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| 广播消息  | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Tag 消息 | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ |

---

# 使用手册

## Java

将 `v4/java/`、`v5/java/grpc/` 或 `v5/java/remoting/` 作为项目根目录导入 IDE 即可。

### 填写连接信息

**Java 原生 SDK（v4/remoting、v5/remoting）**

修改 `common/ClientCreater.java` 中的三个常量：

```java
public static final String NAMESERVER = "腾讯云控制台 > 接入点 > 复制接入地址";  // 格式：rmq-xxx.rocketmq.xx.tencenttdmq.com:9876
public static final String ACCESS_KEY = "角色名称";   // 控制台 > 角色管理
public static final String SECRET_KEY = "角色密钥";   // 控制台 > 角色管理
```

**Java 原生 SDK（v5/grpc）**

每个 Producer/Consumer 文件顶部均有以下配置，按注释填写：

```java
String accessKey = "角色名称";    // 控制台 > 角色管理
String secretKey = "角色密钥";    // 控制台 > 角色管理
String endpoints = "rmq-xxx.rocketmq.xx.tencenttdmq.com:8080";  // 控制台 > 接入点（gRPC 端口 8080）
```

**SpringBoot gRPC（v5/grpc/springboot5-grpc）**

基于 `rocketmq-v5-client-spring-boot-starter`（gRPC 协议），修改 `src/main/resources/application.yml`：

```yaml
rocketmq:
  producer:
    endpoints: rmq-xxx.rocketmq.xx.tencenttdmq.com:8080  # 控制台 > 接入点（gRPC 端口 8080）
    access-key: 角色密钥
    secret-key: 角色名称
    ssl-enabled: false
  push-consumer:
    endpoints: rmq-xxx.rocketmq.xx.tencenttdmq.com:8080
    access-key: 角色密钥
    secret-key: 角色名称
```

> **提示：** 消费者的 SSL 开关只能在 `@RocketMQMessageListener(sslEnabled = false)` 上设置。各场景的 Topic、消费组见该模块 README。

**SpringBoot Remoting（v4/remoting/springboot、v5/remoting/springboot、v5/grpc/springboot5）**

修改 `src/main/resources/application.yml`：

```yaml
rocketmq:
  name-server: rmq-xxx.rocketmq.xx.tencenttdmq.com:9876  # v4/v5 remoting 端口 9876；v5 grpc 端口 8080
  producer:
    access-key: 角色名称
    secret-key: 角色密钥
  consumer:
    access-key: 角色名称
    secret-key: 角色密钥
```

> **提示：** v5/grpc/springboot5 虽然在 v5/grpc 目录下，但 `rocketmq-spring-boot-starter` 内部仍使用 Remoting 协议，
> name-server 填写 9876 端口地址即可。

**Spring Cloud Stream（v4/remoting/springcloud_stream）**

修改 `src/main/resources/application.yml` 中 `spring.cloud.stream.rocketmq.binder` 下的配置：

```yaml
spring:
  cloud:
    stream:
      rocketmq:
        binder:
          name-server: rmq-xxx.rocketmq.xx.tencenttdmq.com:9876
          access-key: 角色名称
          secret-key: 角色密钥
```

> **依赖说明：** Spring Cloud Stream 示例使用了 `com.alibaba.fastjson`，
> 请确保 pom.xml 中包含 `fastjson` 依赖，或将消息体替换为 `String` 类型。

