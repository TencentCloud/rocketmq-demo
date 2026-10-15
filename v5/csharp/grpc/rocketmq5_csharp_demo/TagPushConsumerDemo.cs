using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;


namespace examples
{
    public class TagPushConsumerExample
    {
        private static readonly ILogger Logger = MqLogManager.CreateLogger(typeof(TagPushConsumerExample).FullName);


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


            // 控制台创建的消费组
            const string consumerGroup = "yourConsumerGroup";
            // 控制台创建的「普通消息」类型 Topic 名称，与 ProducerTagMessageDemo.cs 一致
            const string topic = "yourNormalTopic";
            // Tag 过滤表达式：只消费 TagA 或 TagB 的消息，TagC 的消息不会投递给该消费组
            // 订阅全部 Tag 用 "*"；同一消费组内所有消费者的订阅关系必须一致
            var subscription = new Dictionary<string, FilterExpression>
                { { topic, new FilterExpression("TagA||TagB", ExpressionType.Tag) } };


            var pushConsumer = await new PushConsumer.Builder()
                .SetClientConfig(clientConfig)
                .SetConsumerGroup(consumerGroup)
                .SetSubscriptionExpression(subscription)
                .SetMessageListener(new CustomMessageListener())
                .Build();


            Thread.Sleep(Timeout.Infinite);


            // Close the push consumer if you don't need it anymore.
            // await pushConsumer.DisposeAsync();
        }


        private class CustomMessageListener : IMessageListener
        {
            public ConsumeResult Consume(MessageView messageView)
            {
                // Handle the received message and return consume result.
                Logger.LogInformation($"Consume message, tag={messageView.Tag}, messageId={messageView.MessageId}");
                return ConsumeResult.SUCCESS;
            }
        }
    }
}
