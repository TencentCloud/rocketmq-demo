该项目是为了在springboot rocketmq-starter的场景下，使用腾讯云rocketmq4实例。

## 示例场景

启动 `DemoApplication` 后（默认端口 8082），通过以下接口发送消息：

| 场景 | 接口 | 生产者 | 消费者 | 腾讯云 Topic 类型 |
| --- | --- | --- | --- | --- |
| 同步/异步/单向/批量 | `/syncSend` `/asyncSend` `/onewaySend` `/batchSend` | `MessageProducer` | `Tag1Consumer` / `Tag2Consumer` | 普通消息 |
| 延迟消息 | `/delaySend` | `MessageProducer` | `Tag1Consumer` / `Tag2Consumer` | 普通消息 |
| 顺序消息 | `/syncSendOrderly` | `MessageProducer` | `OrderlyConsumer` | 顺序消息 |
| 事务消息 | `/transactionSend` | `TransactionProducer` + `TransactionListenerImpl` | `TransactionConsumer` | 事务消息 |
| 重试消息 | `/retrySend` | `RetryProducer` | `RetryConsumer` | 普通消息 |
| 广播消息 | `/broadcastSend` | `BroadcastProducer` | `BroadcastConsumer` | 普通消息 |

顺序、重试、事务、广播场景使用的 Topic 和消费组在 `application.yml` 的 `rocketmq.order`、`rocketmq.retry`、`rocketmq.transaction`、`rocketmq.broadcast` 下配置，需要先在腾讯云控制台创建。

- **事务消息**：`TransactionListenerImpl` 用 `@RocketMQTransactionListener` 绑定到默认的 `rocketMQTemplate`，本地事务依次模拟 COMMIT、ROLLBACK、UNKNOWN，UNKNOWN 的消息由 Broker 回查 `checkLocalTransaction` 后提交；只有提交的消息会被 `TransactionConsumer` 消费。
- **重试消息**：`RetryConsumer` 在 `onMessage` 中抛异常触发重试并打印 `reconsumeTimes`，前 2 次失败、第 3 次成功；`maxReconsumeTimes = 3`（不设置时默认 16 次），超过后进入死信队列 `%DLQ%消费组`。
- **广播消息**：`BroadcastConsumer` 设置 `messageModel = MessageModel.BROADCASTING`，同组每个实例都会收到全部消息，可以修改 `server.port` 启动多个实例观察；广播模式消费失败不重试。
