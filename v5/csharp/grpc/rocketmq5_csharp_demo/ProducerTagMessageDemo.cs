using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;
namespace examples
{
    internal static class ProducerTagMessageDemo
    {
        static readonly ILoggerFactory factory = LoggerFactory.Create(builder => builder.AddConsole());
        static ILogger logger = factory.CreateLogger("Program_TagProducer");
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

            // 控制台创建的「普通消息」类型 Topic 名称
            const string topic = "yourNormalTopic";
            // In most case, you don't need to create too many producers, single pattern is recommended.
            var producer = await new Producer.Builder()
                // Set the topic name(s), which is optional but recommended.
                // It makes producer could prefetch the topic route before message publishing.
                .SetTopics(topic)
                .SetClientConfig(clientConfig)
                .Build();

            // 同一个 Topic 下用不同 Tag 区分消息子类型，消费端可按 Tag 过滤（见 TagPushConsumerDemo.cs）
            // Tag 不需要在控制台提前创建；一条消息只能设置一个 Tag，且不能包含 "|"
            var tags = new[] { "TagA", "TagB", "TagC" };
            foreach (var tag in tags)
            {
                // Define your message body.
                var bytes = Encoding.UTF8.GetBytes($"This is a tag message, tag={tag}");
                var message = new Message.Builder()
                    .SetTopic(topic)
                    .SetBody(bytes)
                    .SetTag(tag)
                    // You could set multiple keys for the single message actually.
                    .SetKeys($"yourMessageKey-{tag}")
                    .Build();

                try
                {
                    var sendReceipt = await producer.Send(message);
                    logger.LogInformation($"Send message successfully, tag={tag}, messageId={sendReceipt.MessageId}");
                }
                catch (Exception ex)
                {
                    logger.LogError(ex, $"Failed to send message, tag={tag}");
                }
            }

            // Close the producer if you don't need it anymore.
            await producer.DisposeAsync();
        }
    }
}
