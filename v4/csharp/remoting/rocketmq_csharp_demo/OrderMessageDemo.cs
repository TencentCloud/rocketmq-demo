using NewLife;
using NewLife.Log;
using NewLife.RocketMQ;
using NewLife.RocketMQ.Protocol;

namespace rocketmq_csharp_demo
{
    /// <summary>
    /// 顺序消息示例
    /// 生产者按业务 key（这里是订单号）选择固定队列发送，同一订单的消息进入同一个队列；
    /// 消费者开启 OrderConsume，消费前先向 Broker 锁定队列，每个队列单线程按位点顺序消费。
    /// 腾讯云上 Topic 类型需要选择「顺序消息」。
    /// </summary>
    public static class OrderMessageDemo
    {
        public static void RunProducer()
        {
            var producer = new Producer
            {
                // 腾讯云控制台创建的 Topic（顺序类型），需拼接「集群ID|命名空间%Topic名称」
                Topic = "rocketmq-xxx|namespace%topic1",
                // 生产者组，同样拼接「集群ID|命名空间%Group名称」
                Group = "rocketmq-xxx|namespace%group1",
                // 腾讯云控制台提供的接入地址
                NameServerAddress = "rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876",
                Log = XTrace.Log,
                // 腾讯云控制台「角色管理」中的角色名称 / 角色密钥
                CloudProvider = new AclProvider
                {
                    AccessKey = "yourAccessKey",
                    SecretKey = "yourSecretKey",
                },
            };

            producer.Start();

            try
            {
                // 取 Topic 的全部可写队列，按 Broker 名称 + 队列号排序，保证同一 key 每次算出同一个队列
                var queues = producer.Brokers
                    .Where(e => e.IsMaster && e.Permission.HasFlag(Permissions.Write))
                    .SelectMany(e => Enumerable.Range(0, e.WriteQueueNums).Select(i => new MessageQueue { BrokerName = e.Name, QueueId = i }))
                    .ToList();
                if (queues.Count == 0) throw new InvalidOperationException("没有可写的队列，请检查 Topic 是否已创建");

                var steps = new[] { "创建", "支付", "发货", "完成" };
                for (var orderId = 0; orderId < 3; orderId++)
                {
                    // 按订单号取模选择队列，同一订单的所有步骤都发到同一个队列
                    var queue = queues[orderId % queues.Count];
                    foreach (var step in steps)
                    {
                        var message = new Message
                        {
                            Tags = "TagA",
                            Keys = "order" + orderId,
                        };
                        message.SetBody($"订单{orderId} {step}");

                        try
                        {
                            var result = producer.Publish(message, queue);
                            XTrace.WriteLine($"发送成功，订单{orderId} {step}，状态 {result.Status}，队列 {result.Queue}");
                        }
                        catch (Exception ex)
                        {
                            // 顺序消息发送失败时，不要继续发送该订单的后续步骤，否则顺序会被打乱
                            XTrace.WriteLine($"发送失败，订单{orderId} {step}：{ex.Message}");
                            break;
                        }
                    }
                }
            }
            finally
            {
                producer.Dispose();
            }
        }

        public static void RunConsumer()
        {
            var consumer = new Consumer
            {
                // 腾讯云控制台创建的 Topic（顺序类型），需拼接「集群ID|命名空间%Topic名称」
                Topic = "rocketmq-xxx|namespace%topic1",
                // 腾讯云控制台创建的消费组，需拼接「集群ID|命名空间%Group名称」
                Group = "rocketmq-xxx|namespace%group1",
                NameServerAddress = "rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876",
                // 顺序消费：消费前锁定队列，同一时刻一个队列只由一个消费者消费
                OrderConsume = true,
                // 顺序消费失败时不能把消息发回重试队列（会打乱顺序），关闭 SDK 的自动发回；
                // 此时 OnConsume 返回 false 会从同一位点重新拉取这批消息，阻塞该队列后续消息
                EnableRetry = false,
                Log = XTrace.Log,
                CloudProvider = new AclProvider
                {
                    AccessKey = "yourAccessKey",
                    SecretKey = "yourSecretKey",
                },
            };

            consumer.OnConsume = (q, ms) =>
            {
                // 同一队列的消息按发送顺序依次到达
                foreach (var item in ms)
                {
                    XTrace.WriteLine($"[{q.BrokerName}@{q.QueueId}]收到顺序消息，Key {item.Keys}，队列位点 {item.QueueOffset}，内容 {item.Body.ToStr()}");
                }

                return true;
            };

            consumer.Start();

            // 等待消费，测试用，真实的场景不需要
            Thread.Sleep(60_000);

            consumer.Dispose();
        }
    }
}
