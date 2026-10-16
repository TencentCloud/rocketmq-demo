using NewLife.Log;
using NewLife.RocketMQ;
using NewLife.RocketMQ.Protocol;

namespace rocketmq_csharp_demo
{
    /// <summary>
    /// 延迟消息（固定延迟级别）示例
    /// 通过 Message.DelayTimeLevel 设置延迟级别；腾讯云任意时间定时消息见 TimerMessageDemo。
    /// 级别与时间对应关系：
    /// 1s、 5s、 10s、 30s、 1m、 2m、 3m、 4m、 5m、 6m、 7m、 8m、 9m、 10m、 20m、 30m、 1h、 2h
    /// 1    2    3     4     5    6   7    8   9    10   11   12  13   14    15    16   17   18
    /// 消费端与普通消息相同，可直接使用 Program.cs 中的消费者订阅同一个 Topic。
    /// </summary>
    public static class DelayMessageDemo
    {
        public static void Run()
        {
            var producer = new Producer
            {
                // 腾讯云控制台创建的 Topic，需拼接「集群ID|命名空间%Topic名称」
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
                for (var i = 0; i < 5; i++)
                {
                    var message = new Message
                    {
                        Tags = "TagA",
                        Keys = "delayKey" + i,
                        // 延迟级别 3 = 10 秒后投递
                        DelayTimeLevel = 3,
                    };
                    message.SetBody("延迟消息测试" + i);

                    try
                    {
                        // 第二个参数为 null 表示由 SDK 自动选择队列（SDK 未标注可空，用 null! 避免可空警告）
                        var result = producer.Publish(message, null!);
                        XTrace.WriteLine($"发送延迟消息成功，状态 {result.Status}，MsgId {result.MsgId}，队列 {result.Queue}");
                    }
                    catch (Exception ex)
                    {
                        XTrace.WriteLine($"发送延迟消息失败：{ex.Message}");
                    }
                }
            }
            finally
            {
                producer.Dispose();
            }
        }
    }
}
