using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;
namespace examples
{
    internal static class ProducerFifoMessageDemo
    {
        static readonly ILoggerFactory factory = LoggerFactory.Create(builder => builder.AddConsole());
        static ILogger logger = factory.CreateLogger("Program_FifoProducer");
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

            // 控制台创建的「顺序消息」类型 Topic 名称；消费端的消费组需开启「顺序投递」才能按顺序消费
            const string topic = "yourFifoTopic";
            // In most case, you don't need to create too many producers, single pattern is recommended.
            var producer = await new Producer.Builder()
                // Set the topic name(s), which is optional but recommended.
                // It makes producer could prefetch the topic route before message publishing.
                .SetTopics(topic)
                .SetClientConfig(clientConfig)
                .Build();

            // 同一个 MessageGroup 内的消息按发送顺序投递，不同 MessageGroup 之间互不影响
            // 通常用业务主键（如订单号）作为 MessageGroup
            const string messageGroup = "yourMessageGroup";
            for (var i = 0; i < 5; i++)
            {
                // Define your message body.
                var bytes = Encoding.UTF8.GetBytes($"This is a fifo message, index={i}");
                var message = new Message.Builder()
                    .SetTopic(topic)
                    .SetBody(bytes)
                    .SetTag("yourMessageTagA")
                    // You could set multiple keys for the single message actually.
                    .SetKeys($"yourMessageKey-fifo-{i}")
                    // 设置 MessageGroup。注意：不能与 DeliveryTimestamp、LiteTopic、Priority 同时使用
                    .SetMessageGroup(messageGroup)
                    .Build();

                try
                {
                    // 顺序消息需要同步发送：上一条发送成功后再发下一条，才能保证顺序
                    var sendReceipt = await producer.Send(message);
                    logger.LogInformation($"Send fifo message successfully, messageGroup={messageGroup}, index={i}, messageId={sendReceipt.MessageId}");
                }
                catch (Exception ex)
                {
                    logger.LogError(ex, $"Failed to send fifo message, messageGroup={messageGroup}, index={i}");
                }
            }

            // Close the producer if you don't need it anymore.
            await producer.DisposeAsync();
        }
    }
}
