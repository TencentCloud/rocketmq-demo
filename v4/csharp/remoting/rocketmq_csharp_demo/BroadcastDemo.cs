using NewLife;
using NewLife.Log;
using NewLife.RocketMQ;
using NewLife.RocketMQ.Models;

namespace rocketmq_csharp_demo
{
    /// <summary>
    /// 广播消息示例
    /// 消费者设置 MessageModel = Broadcasting 后，同一消费组的每个实例都会收到 Topic 的全部消息。
    /// 可启动多个进程分别调用 RunConsumer，再调用 RunProducer 发送消息，每个进程都会打印全部消息。
    /// 广播模式的消费位点保存在本地（默认当前目录 .offsets/消费组.json，可用 OffsetStorePath 修改）。
    /// 广播模式不支持消费重试，消息不会进入重试队列和死信队列。
    /// </summary>
    public static class BroadcastDemo
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
                for (var i = 0; i < 10; i++)
                {
                    try
                    {
                        var result = producer.Publish("广播消息测试" + i, "TagA", "broadcastKey" + i);
                        XTrace.WriteLine($"发送成功，状态 {result.Status}，MsgId {result.MsgId}，队列 {result.Queue}");
                    }
                    catch (Exception ex)
                    {
                        XTrace.WriteLine($"发送失败：{ex.Message}");
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
                // 广播模式，默认是集群模式 Clustering
                MessageModel = MessageModels.Broadcasting,
                // 广播模式不支持消费重试，关闭 SDK 在消费失败时把消息发回 Broker 重试队列的行为
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
                foreach (var item in ms)
                {
                    XTrace.WriteLine($"[{q.BrokerName}@{q.QueueId}]收到广播消息，Key {item.Keys}，内容 {item.Body.ToStr()}");
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
