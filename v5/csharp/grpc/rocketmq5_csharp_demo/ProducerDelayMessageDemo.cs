using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;
namespace examples
{
    internal static class ProducerDelayMessageDemo
    {
        static readonly ILoggerFactory factory = LoggerFactory.Create(builder => builder.AddConsole());
        static ILogger logger = factory.CreateLogger("Program_DelayProducer");
        internal static async Task QuickStart()
        {
            // Enable the switch if you use .NET Core 3.1 and want to disable TLS/SSL.
            AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
            // 控制台「访问管理」中角色的 AccessKey / SecretKey
            const string accessKey = "yourAccessKey";
            const string secretKey = "yourSecretKey";

            // Credential provider is optional for client configuration.
            var credentialsProvider = new StaticSessionCredentialsProvider(accessKey, secretKey);
            const string endpoints = "rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"; // 腾讯云接入点
            var clientConfig = new ClientConfig.Builder()
                .SetEndpoints(endpoints)
                .SetCredentialsProvider(credentialsProvider)
                .Build();

            // 控制台创建的「定时/延时消息」类型 Topic 名称
            const string topic = "yourDelayTopic";
            // In most case, you don't need to create too many producers, single pattern is recommended.
            var producer = await new Producer.Builder()
                // Set the topic name(s), which is optional but recommended.
                // It makes producer could prefetch the topic route before message publishing.
                .SetTopics(topic)
                .SetClientConfig(clientConfig)
                .Build();

            // 期望投递时间：这里为 30 秒后。延时消息即“当前时间 + 延时时长”的定时消息
            // 最大可设置的定时时长以腾讯云控制台/产品文档说明为准
            var deliveryTimestamp = DateTime.Now + TimeSpan.FromSeconds(30);
            // Define your message body.
            var bytes = Encoding.UTF8.GetBytes("This is a delay message");
            var message = new Message.Builder()
                .SetTopic(topic)
                .SetBody(bytes)
                .SetTag("yourMessageTagA")
                // You could set multiple keys for the single message actually.
                .SetKeys("yourMessageKey-2f00df144e48")
                // 设置投递时间。注意：不能与 MessageGroup、LiteTopic、Priority 同时使用
                .SetDeliveryTimestamp(deliveryTimestamp)
                .Build();

            try
            {
                var sendReceipt = await producer.Send(message);
                logger.LogInformation($"Send delay message successfully, deliveryTimestamp={deliveryTimestamp:O}, messageId={sendReceipt.MessageId}");
            }
            catch (Exception ex)
            {
                logger.LogError(ex, "Failed to send delay message");
            }

            // Close the producer if you don't need it anymore.
            await producer.DisposeAsync();
        }
    }
}
