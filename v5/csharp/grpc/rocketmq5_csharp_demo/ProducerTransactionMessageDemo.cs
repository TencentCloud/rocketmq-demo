using System.Text;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;
namespace examples
{
    internal static class ProducerTransactionMessageDemo
    {
        static readonly ILoggerFactory factory = LoggerFactory.Create(builder => builder.AddConsole());
        static ILogger logger = factory.CreateLogger("Program_TransactionProducer");

        // 事务回查：本地事务提交/回滚结果未送达服务端（如进程崩溃、超时）时，服务端会回调此方法确认事务状态
        private class CustomTransactionChecker : ITransactionChecker
        {
            public TransactionResolution Check(MessageView messageView)
            {
                // 根据消息内容（如 Keys 中的业务主键）反查本地事务状态：
                // Commit 表示投递给消费者，Rollback 表示丢弃，Unknown 表示稍后再回查
                logger.LogInformation($"Receive transaction check, messageId={messageView.MessageId}, keys=[{string.Join(", ", messageView.Keys)}]");
                return TransactionResolution.Commit;
            }
        }

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

            // 控制台创建的「事务消息」类型 Topic 名称
            const string topic = "yourTransactionTopic";
            // In most case, you don't need to create too many producers, single pattern is recommended.
            var producer = await new Producer.Builder()
                // Set the topic name(s), which is optional but recommended.
                // It makes producer could prefetch the topic route before message publishing.
                .SetTopics(topic)
                .SetClientConfig(clientConfig)
                // 事务消息必须设置回查器
                .SetTransactionChecker(new CustomTransactionChecker())
                .Build();

            var transaction = producer.BeginTransaction();
            // Define your message body.
            var bytes = Encoding.UTF8.GetBytes("This is a transaction message");
            var message = new Message.Builder()
                .SetTopic(topic)
                .SetBody(bytes)
                .SetTag("yourMessageTagA")
                // 建议把业务主键放在 Keys 中，便于回查时定位本地事务
                .SetKeys("yourOrderId-7044358f98fc")
                .Build();

            try
            {
                // 发送半消息，此时消息对消费者不可见
                var sendReceipt = await producer.Send(message, transaction);
                logger.LogInformation($"Send transaction message successfully, messageId={sendReceipt.MessageId}");

                // 执行本地事务（如写数据库），根据结果提交或回滚
                var localTransactionSucceeded = true;
                if (localTransactionSucceeded)
                {
                    // 提交后消息才会投递给消费者
                    await transaction.Commit();
                    logger.LogInformation($"Transaction committed, messageId={sendReceipt.MessageId}");
                }
                else
                {
                    await transaction.Rollback();
                    logger.LogInformation($"Transaction rolled back, messageId={sendReceipt.MessageId}");
                }
            }
            catch (Exception ex)
            {
                // 发送失败则消息不存在；提交/回滚失败则由服务端回查 CustomTransactionChecker 决定最终状态
                logger.LogError(ex, "Failed to send or end transaction message");
            }

            // 实际业务中生产者应长期运行，否则关闭后无法响应服务端的事务回查
            // Close the producer if you don't need it anymore.
            await producer.DisposeAsync();
        }
    }
}
