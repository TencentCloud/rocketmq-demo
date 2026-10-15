using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;
using Org.Apache.Rocketmq.Error;


namespace examples
{
    public class LitePushConsumerExample
    {
        private static readonly ILogger Logger = MqLogManager.CreateLogger(typeof(LitePushConsumerExample).FullName);


        internal static async Task QuickStart()
        {
            const string accessKey = "yourAccessKey";
            const string secretKey = "yourSecretKey";
            // Enable the switch if you use .NET Core 3.1 and want to disable TLS/SSL.
            // AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);


            // Credential provider is optional for client configuration.
            var credentialsProvider = new StaticSessionCredentialsProvider(accessKey, secretKey);
            const string endpoints = "rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"; // 腾讯云接入点
            var clientConfig = new ClientConfig.Builder()
                .SetEndpoints(endpoints)
                .SetCredentialsProvider(credentialsProvider)
                .Build();


            // 控制台创建的消费组，需选择「Lite Topic 消费」模式并绑定到下面的一级 Topic
            const string consumerGroup = "yourConsumerGroup";
            // 控制台创建的「轻量消息」类型的一级 Topic 名称
            const string bindTopic = "yourParentTopic";


            var litePushConsumer = await new LitePushConsumer.Builder()
                .SetClientConfig(clientConfig)
                .SetConsumerGroup(consumerGroup)
                // 绑定一级 Topic，后续订阅的 lite topic 都属于该 Topic
                .SetBindTopic(bindTopic)
                .SetMessageListener(new CustomMessageListener())
                .Build();


            try
            {
                // SubscribeLite() 会发起网络请求并校验配额，可能失败，需要检查结果：
                // 1. 网络请求失败，可以稍后重试；
                // 2. 配额校验失败（LiteSubscriptionQuotaExceededException），需要评估配额是否足够，
                //    并及时通过 UnsubscribeLite() 取消不再使用的订阅以释放配额。
                // 默认从最新位点开始消费
                await litePushConsumer.SubscribeLite("lite-topic-0");
                // 从最早位点开始消费
                await litePushConsumer.SubscribeLite("lite-topic-1", OffsetOption.MinOffset);
                // 从最后 100 条消息开始消费
                await litePushConsumer.SubscribeLite("lite-topic-2", OffsetOption.OfTailN(100));
                Logger.LogInformation($"Subscribed lite topics=[{string.Join(", ", litePushConsumer.GetLiteTopicSet())}]");
            }
            catch (LiteSubscriptionQuotaExceededException ex)
            {
                // 1. 评估并提升 lite topic 订阅配额
                // 2. 及时取消不再使用的订阅，例如：await litePushConsumer.UnsubscribeLite("lite-topic-2");
                Logger.LogError(ex, "Lite subscription quota exceeded");
            }
            catch (Exception ex)
            {
                // 稍后重试
                Logger.LogError(ex, "Failed to subscribe lite topic");
            }


            Thread.Sleep(Timeout.Infinite);


            // Close the lite push consumer if you don't need it anymore.
            // await litePushConsumer.DisposeAsync();
        }


        private class CustomMessageListener : IMessageListener
        {
            public ConsumeResult Consume(MessageView messageView)
            {
                // Handle the received message and return consume result.
                // 注意：RocketMQ.Client 5.2.1 的 MessageView 暂未暴露 LiteTopic 属性
                Logger.LogInformation($"Consume message={messageView}");
                return ConsumeResult.SUCCESS;
            }
        }
    }
}
