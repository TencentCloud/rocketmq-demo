using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;
namespace examples
{
    internal static class ProducerLiteMessageDemo
    {
        static readonly ILoggerFactory factory = LoggerFactory.Create(builder => builder.AddConsole());
        static ILogger logger = factory.CreateLogger("Program_LiteProducer");
        internal static async Task QuickStart()
        {
            // Enable the switch if you use .NET Core 3.1 and want to disable TLS/SSL.
            AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
            const string accessKey = "yourAccessKey";
            const string secretKey = "yourSecretKey";

            // Credential provider is optional for client configuration.
            var credentialsProvider = new StaticSessionCredentialsProvider(accessKey, secretKey);
            const string endpoints = "rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"; // 腾讯云接入点
            var clientConfig = new ClientConfig.Builder()
                .SetEndpoints(endpoints)
                .SetCredentialsProvider(credentialsProvider)
                .Build();

            // 控制台创建的「轻量消息」类型的一级 Topic 名称
            const string parentTopic = "yourParentTopic";
            // In most case, you don't need to create too many producers, single pattern is recommended.
            var producer = await new Producer.Builder()
                // Set the topic name(s), which is optional but recommended.
                // It makes producer could prefetch the topic route before message publishing.
                .SetTopics(parentTopic)
                .SetClientConfig(clientConfig)
                .Build();

            // 每条消息可以指定不同的 lite topic（二级 Topic），无需提前在控制台创建
            var liteTopics = new[] { "lite-topic-0", "lite-topic-1", "lite-topic-2" };
            foreach (var liteTopic in liteTopics)
            {
                // Define your message body.
                var bytes = Encoding.UTF8.GetBytes($"This is a lite message, liteTopic={liteTopic}");
                var message = new Message.Builder()
                    .SetTopic(parentTopic)
                    .SetBody(bytes)
                    // You could set multiple keys for the single message actually.
                    .SetKeys($"yourMessageKey-{liteTopic}")
                    // 设置 lite topic。注意：不能与 MessageGroup、DeliveryTimestamp、Priority 同时使用
                    .SetLiteTopic(liteTopic)
                    .Build();

                try
                {
                    var sendReceipt = await producer.Send(message);
                    logger.LogInformation($"Send message successfully, liteTopic={liteTopic}, messageId={sendReceipt.MessageId}");
                }
                catch (Exception ex)
                {
                    // 如果 lite topic 数量超过配额，需要评估并提升 lite topic 资源上限
                    logger.LogError(ex, $"Failed to send message, liteTopic={liteTopic}");
                }
            }

            // Close the producer if you don't need it anymore.
            await producer.DisposeAsync();
        }
    }
}
