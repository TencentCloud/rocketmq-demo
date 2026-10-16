using NewLife;
using NewLife.Log;
using NewLife.RocketMQ;

namespace rocketmq_csharp_demo
{
    /// <summary>
    /// Tag 消息示例
    /// 生产者分别发送 TagA / TagB / TagC 三种 Tag 的消息，消费者只订阅 "TagA || TagB"，TagC 的消息不会被该消费组收到。
    /// 同一个消费组内所有消费者的订阅关系必须保持一致。
    /// </summary>
    public static class TagDemo
    {
        public static void RunProducer()
        {
            var producer = new Producer
            {
                // 腾讯云控制台创建的 Topic（普通类型），需拼接「集群ID|命名空间%Topic名称」
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
                foreach (var tag in new[] { "TagA", "TagB", "TagC" })
                {
                    for (var i = 0; i < 3; i++)
                    {
                        try
                        {
                            // Publish(消息体, Tag, Key)
                            var result = producer.Publish($"Tag消息测试 {tag}-{i}", tag, $"{tag}-key{i}");
                            XTrace.WriteLine($"发送成功，Tag {tag}，状态 {result.Status}，MsgId {result.MsgId}，队列 {result.Queue}");
                        }
                        catch (Exception ex)
                        {
                            XTrace.WriteLine($"发送失败，Tag {tag}：{ex.Message}");
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
                // 腾讯云控制台创建的 Topic，需拼接「集群ID|命名空间%Topic名称」
                Topic = "rocketmq-xxx|namespace%topic1",
                // 腾讯云控制台创建的消费组，需拼接「集群ID|命名空间%Group名称」
                Group = "rocketmq-xxx|namespace%group1",
                NameServerAddress = "rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876",
                // 订阅表达式：只消费 TagA 或 TagB，"*" 表示订阅全部 Tag
                Subscription = "TagA || TagB",
                Log = XTrace.Log,
                CloudProvider = new AclProvider
                {
                    AccessKey = "yourAccessKey",
                    SecretKey = "yourSecretKey",
                },
            };

            consumer.OnConsume = (q, ms) =>
            {
                foreach (var item in ms)
                {
                    XTrace.WriteLine($"[{q.BrokerName}@{q.QueueId}]收到消息，Tag {item.Tags}，Key {item.Keys}，内容 {item.Body.ToStr()}");
                }

                // 返回 true 表示消费成功
                return true;
            };

            consumer.Start();

            // 等待消费，测试用，真实的场景不需要
            Thread.Sleep(60_000);

            consumer.Dispose();
        }
    }
}
