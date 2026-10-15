该项目演示在 SpringBoot 中使用 `rocketmq-v5-client-spring-boot-starter`（gRPC 协议）访问腾讯云 RocketMQ 5.x 实例。

- starter：`org.apache.rocketmq:rocketmq-v5-client-spring-boot-starter:2.3.6`（SpringBoot 2.7.18，Java 8）
- 与 `../springboot5`（rocketmq-spring-boot-starter，Remoting 协议）的区别：这里使用 `RocketMQClientTemplate` 和 gRPC 接入点。

## 使用步骤

1. 在腾讯云控制台创建角色并授权，创建下表中的 Topic 和消费组。
2. 修改 `src/main/resources/application.yml`：接入地址、角色密钥、角色名称、Topic、消费组。
3. 启动 `DemoApplication`，访问下表中的接口触发发送，在日志中查看消费结果。

| 场景 | Topic 类型 | 触发接口 | 生产者 | 消费者 |
| --- | --- | --- | --- | --- |
| 普通消息同步发送 | 普通 | `GET /normal/syncSend` | `NormalMessageProducer` | `NormalMessageConsumer` |
| 普通消息异步发送 | 普通 | `GET /normal/asyncSend` | `NormalMessageProducer` | `NormalMessageConsumer` |
| Tag 消息 | 普通 | `GET /tag/send` | `TagMessageProducer` | `TagMessageConsumer` |
| 顺序消息 | 顺序（消费组开启顺序投递） | `GET /fifo/send` | `FifoMessageProducer` | `FifoMessageConsumer` |
| 定时/延迟消息 | 定时/延时 | `GET /delay/send` | `DelayMessageProducer` | `DelayMessageConsumer` |
| 事务消息 | 事务 | `GET /transaction/send` | `TransactionMessageProducer` + `TransactionMessageChecker` | `TransactionMessageConsumer` |
| 重试消息 | 普通 | `GET /retry/send` | `RetryMessageProducer` | `RetryMessageConsumer` |

## 注意

- starter 默认开启 SSL：生产者用 `rocketmq.producer.ssl-enabled: false` 关闭，消费者在 `@RocketMQMessageListener(sslEnabled = false)` 上关闭，与 Java 原生示例一致。
- 事务消息：starter 2.3.6 的 `@RocketMQTransactionListener` 只提供回查（`RocketMQTransactionChecker#check`），本地事务在 `sendTransactionMessage` 返回后由业务代码执行，再调用 `commit()` / `rollback()`。
- 重试消息：消费者返回 `ConsumeResult.FAILURE` 即触发重试；最大重试次数由消费组的重试策略决定，超过后进入死信。`rocketmq.producer.max-attempts` 是发送失败重试，与消费重试无关。
- 不支持：广播消费（5.x gRPC SDK 不支持）；Lite Topic（starter 2.3.6 依赖的 rocketmq-client-java 5.0.7 无 Lite 相关 API）。
