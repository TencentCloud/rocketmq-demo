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
│   ├── rust/
│   └── csharp/
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

# 5.x 已完成 demo

## gRPC 协议

| 特性    | Java 原生 | SpringBoot | CPP | Go | Rust | Node.js | C# |
|:------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 普通消息  | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 重试消息  | ✅ | ❌ | ✅ | ✅ | | ❌ | ❌ |
| 有序消息  | ✅ | ❌ | ✅ | ✅ | | ❌ | ❌ |
| 事务消息  | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ |
| 广播消息  | ✅ | ❌ | ❌ | ❌ | | ❌ | ❌ |
| Tag 消息 | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ |

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

**SpringBoot（v4/remoting/springboot、v5/remoting/springboot、v5/grpc/springboot5）**

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

