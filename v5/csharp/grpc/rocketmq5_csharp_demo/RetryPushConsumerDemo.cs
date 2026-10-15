using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;


namespace examples
{
    public class RetryPushConsumerExample
    {
        private static readonly ILogger Logger = MqLogManager.CreateLogger(typeof(RetryPushConsumerExample).FullName);


        internal static async Task QuickStart()
        {
            // 控制台「访问管理」中角色的 AccessKey / SecretKey
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


            // 控制台创建的消费组。最大重试次数由该消费组的重试策略决定（控制台消费组配置），
            // 超过最大重试次数仍消费失败的消息会进入死信，不再投递
            const string consumerGroup = "yourConsumerGroup";
            // 控制台创建的 Topic 名称
            const string topic = "yourTopic";
            var subscription = new Dictionary<string, FilterExpression>
                { { topic, new FilterExpression("*") } };


            var pushConsumer = await new PushConsumer.Builder()
                .SetClientConfig(clientConfig)
                .SetConsumerGroup(consumerGroup)
                .SetSubscriptionExpression(subscription)
                .SetMessageListener(new RetryMessageListener())
                .Build();


            Thread.Sleep(Timeout.Infinite);


            // Close the push consumer if you don't need it anymore.
            // await pushConsumer.DisposeAsync();
        }


        private class RetryMessageListener : IMessageListener
        {
            // 演示用：前几次投递返回失败，第 SucceedOnAttempt 次投递时返回成功
            private const int SucceedOnAttempt = 3;

            public ConsumeResult Consume(MessageView messageView)
            {
                // DeliveryAttempt 表示第几次投递，首次投递为 1，每次重试加 1
                var deliveryAttempt = messageView.DeliveryAttempt;
                if (deliveryAttempt < SucceedOnAttempt)
                {
                    // 返回 FAILURE（或在此抛出异常）即触发重试：
                    // 普通消息会在退避时间后重新投递；顺序消息会在客户端本地重试，保证同一 MessageGroup 的顺序
                    // 注意：生产者的 SetMaxAttempts 是发送失败重试，与这里的消费重试无关
                    Logger.LogWarning($"Consume failed, will retry later, messageId={messageView.MessageId}, deliveryAttempt={deliveryAttempt}");
                    return ConsumeResult.FAILURE;
                }

                Logger.LogInformation($"Consume succeeded, messageId={messageView.MessageId}, deliveryAttempt={deliveryAttempt}");
                return ConsumeResult.SUCCESS;
            }
        }
    }
}
