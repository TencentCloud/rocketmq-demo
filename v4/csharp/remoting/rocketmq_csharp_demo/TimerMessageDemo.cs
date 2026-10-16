using NewLife.Log;
using NewLife.RocketMQ;
using NewLife.RocketMQ.Protocol;

namespace rocketmq_csharp_demo
{
    /// <summary>
    /// 定时消息（任意时间）示例
    /// 通过消息属性 __STARTDELIVERTIME 指定投递时间（毫秒时间戳），这是腾讯云 TDMQ RocketMQ 4.x 的定时消息属性，
    /// 开源 Apache RocketMQ Broker 不识别该属性，消息会立即投递。
    /// 不要同时设置 DelayTimeLevel；若设置的时间早于当前时间，消息将立即投递。
    /// 消费端与普通消息相同，可直接使用 Program.cs 中的消费者订阅同一个 Topic。
    /// </summary>
    public static class TimerMessageDemo
    {
        public static void Run()
        {
            var producer = new Producer
            {
                // 腾讯云控制台创建的 Topic（延迟/定时消息类型），需拼接「集群ID|命名空间%Topic名称」
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
                        Keys = "timerKey" + i,
                    };
                    message.SetBody("定时消息测试" + i);

                    // 30 秒后投递，值为毫秒级 Unix 时间戳
                    var deliverTime = DateTimeOffset.UtcNow.AddSeconds(30).ToUnixTimeMilliseconds();
                    message.PutUserProperty("__STARTDELIVERTIME", deliverTime.ToString());

                    try
                    {
                        // 第二个参数为 null 表示由 SDK 自动选择队列（SDK 未标注可空，用 null! 避免可空警告）
                        var result = producer.Publish(message, null!);
                        XTrace.WriteLine($"发送定时消息成功，投递时间 {DateTimeOffset.FromUnixTimeMilliseconds(deliverTime).LocalDateTime}，状态 {result.Status}，MsgId {result.MsgId}");
                    }
                    catch (Exception ex)
                    {
                        XTrace.WriteLine($"发送定时消息失败：{ex.Message}");
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
