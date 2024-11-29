using NewLife;
using NewLife.Log;
using NewLife.RocketMQ;

// 日志默认在当前的logs目录下面,当前c#不支持轨迹

//生产者
var mq = new Producer
{
    Topic = "TopicTest1", //对于非通用版集群，需要拼接完整的topic，如 MQ_INSTxxx_aaa%TopicTest
    NameServerAddress = "127.0.0.1:9876",// 腾讯云页面填写
    Log = XTrace.Log,
    AclOptions = new AclOptions
    {
        AccessKey = "AKID**********************0123456789",
        SecretKey = "sk0123456789********************",
    },
};

mq.Start();

for (var i = 0; i < 10; i++)
{
    var str = "生产消息测试" + i;
    var sr = mq.Publish(str, "TagA");
}


//消费者

var consumer = new Consumer
{
    Topic = "TopicTest1",//对于非通用版集群，需要拼接完整的topic，如 MQ_INSTxxx_aaa%TopicTest
    Group = "test", //对于非通用版集群，需要拼接完整的topic，如 MQ_INSTxxx_aaa%TopicTest
    NameServerAddress = "127.0.0.1:9876",

    FromLastOffset = true,
    SkipOverStoredMsgCount = 0,
    BatchSize = 20,

    Log = XTrace.Log,
    AclOptions = new AclOptions
    {
        AccessKey = "AKID**********************0123456789",
        SecretKey = "sk0123456789********************",
    },
};

consumer.OnConsume = (q, ms) =>
{
    XTrace.WriteLine("[{0}@{1}]收到消息 [{2}]", q.BrokerName, q.QueueId, ms.Length);

    foreach (var item in ms.ToList())
    {
        XTrace.WriteLine($"消息 {item.Keys}，发送时间{item.BornTimestamp.ToDateTime()}，内容 {item.Body.ToStr()}");
    }

    return true;
};

consumer.Start();

//等2s，测试用，真实的场景不需要
Thread.Sleep(200000);