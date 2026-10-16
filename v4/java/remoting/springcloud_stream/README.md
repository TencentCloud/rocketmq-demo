# Spring Cloud Stream RocketMQ 示例（Remoting 协议）

基于 `spring-cloud-starter-stream-rocketmq`（Spring Cloud Alibaba 2025.0.0.0，内部使用 rocketmq-client 5.3.1 的 Remoting 协议）连接腾讯云 RocketMQ 4.x 实例。

## 配置

修改 `src/main/resources/application.yml` 中 `spring.cloud.stream.rocketmq.binder`：

| 配置项 | 说明 |
|:--|:--|
| `name-server` | 接入地址（9876 端口），在控制台集群基本信息里获取 |
| `access-key` | 角色密钥，在控制台【角色管理】复制 |
| `secret-key` | 角色名称 |
| `namespace` | 命名空间全称，如 `rocketmq-xxx\|namespace1` |
| `group` | 默认生产者组（各 binding 未单独配置 group 时使用） |

以下 Topic 和消费组需要先在控制台创建（Topic 类型见下表）。不需要的场景可以从 `spring.cloud.function.definition` 中去掉对应函数名，就不会启动该消费者。

## 场景

启动 `StreamDemoApplication` 后调用对应接口发送消息，消费日志打印在控制台。

| 场景 | 接口 | 生产 binding | 消费函数 / binding | Topic（类型） | 消费组 | 关键配置 |
|:--|:--|:--|:--|:--|:--|:--|
| 普通 + Tag | `GET /test-simple` | `topicSend-out-0` | `topicTag1-in-0`、`topicTag2-in-0` | `TopicTest`（普通） | `consumer-group1`、`consumer-group2` | 生产设置 `TAGS` 头；消费 `consumer.subscription: TAG1`（多个 tag 用 `TAG1 \|\| TAG2`） |
| 延迟/定时 | `GET /test-delay` | `topicSend-out-0` | 同上 | `TopicTest`（沿用已有配置） | 同上 | `DELAY` 延迟级别头或 `TIMER_DELIVER_MS` 定时头 |
| 重试 | `GET /test-retry` | `retryProducer-out-0` | `retryConsumer` / `retryConsumer-in-0` | `TopicRetry`（普通） | `retry-consumer-group` | `consumer.max-attempts: 1`；`push.max-reconsume-times: 3` |
| 事务 | `GET /test-transaction` | `txProducer-out-0` | `txConsumer` / `txConsumer-in-0` | `TopicTransaction`（事务） | `tx-consumer-group` | `producer.producer-type: Trans`；`producer.transaction-listener: streamTransactionListener` |
| 广播 | `GET /test-broadcast` | `broadcastProducer-out-0` | `broadcastConsumer` / `broadcastConsumer-in-0` | `TopicBroadcast`（普通） | `broadcast-consumer-group` | `consumer.message-model: BROADCASTING` |
| 顺序 | `GET /test-orderly` | `orderlyProducer-out-0` | `orderlyConsumer` / `orderlyConsumer-in-0` | `TopicOrderly`（全局顺序或局部顺序） | `orderly-consumer-group` | `producer.message-queue-selector: orderMessageQueueSelector`；`push.orderly: true` |

> 已有的 `GET /test-order` 只是向普通 Topic 普通发送 100 条消息，不保证顺序；顺序消息请使用 `GET /test-orderly`。

### 重试

- 消费函数抛异常时，binder 返回 `RECONSUME_LATER`，消息进入 `%RETRY%消费组`，服务端按重试次数递增延迟后重新投递；超过 `max-reconsume-times` 后进入死信队列 `%DLQ%消费组`。
- `spring.cloud.stream.rocketmq.bindings.<binding>.consumer.push.max-reconsume-times`：binder 默认值是 **0**（首次失败直接进死信），`-1` 表示 RocketMQ 默认的 16 次，需要重试时必须显式配置。
- `spring.cloud.stream.bindings.<binding>.consumer.max-attempts`：Spring Cloud Stream 的**本地**重试（默认 3），在同一线程立即重放、不回到服务端；全部失败后才触发服务端重试。示例设为 1，只演示服务端重试。
- 如果给 binding 配置了 `error-handler-definition` 或订阅了它的错误通道，异常会被吞掉、消息按成功处理，不会触发服务端重试。
- binder 不把 `reconsumeTimes` 放进 Spring 消息头，示例按 `ROCKET_MQ_MESSAGE_ID`（重试时不变）在内存中统计投递次数，仅用于演示。
- 广播模式下消费失败不重试。生产者的 `retry-times-when-send-failed` 是发送失败重试，与消费重试无关。

### 事务

`TransactionStreamController` 依次发送 3 条半消息，通过 `TRANSACTIONAL_ARGS` 头把模拟的本地事务结果传给 `StreamTransactionListener#executeLocalTransaction`：`COMMIT` 提交、`ROLLBACK` 回滚、`UNKNOWN` 等待服务端回查（`checkLocalTransaction` 返回提交）。消费者只会收到提交的消息。

### 广播

用不同端口启动多个实例（如 `--server.port=8081`），调用 `/test-broadcast` 后每个实例都会收到全部 5 条消息。广播模式消费位点保存在客户端本地。

### 顺序

`OrderMessageQueueSelector` 按消息 `KEYS`（订单号）哈希选择队列，同一订单的“创建/支付/发货/完成”进入同一队列；消费端 `push.orderly: true` 使用 `MessageListenerOrderly` 按队列串行消费。消费失败时暂停当前队列 `suspend-current-queue-time-millis` 后本地重试，超过 `max-reconsume-times` 进入死信队列（binder 默认 0）。
