using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;

namespace examples
{
    internal static class SimpleConsumerExample
    {
        static readonly ILoggerFactory factory = LoggerFactory.Create(builder => builder.AddConsole());
        static ILogger logger = factory.CreateLogger("Program_Consumer");
        internal static async Task QuickStart()
        {
            // Enable the switch if you use .NET Core 3.1 and want to disable TLS/SSL.
            AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
            const string accessKey = "yourAccessKey";
            const string secretKey = "yourSecretKey";

            // Credential provider is optional for client configuration.
            var credentialsProvider = new StaticSessionCredentialsProvider(accessKey, secretKey);
            const string endpoints = "foobar.com:8080";
            var clientConfig = new ClientConfig.Builder()
                .SetEndpoints(endpoints)
                .SetCredentialsProvider(credentialsProvider)
                .Build();

            // Add your subscriptions.
            const string consumerGroup = "yourConsumerGroup";
            const string topic = "yourTopic";
            var subscription = new Dictionary<string, FilterExpression>
                { { topic, new FilterExpression("*") } };
            // In most case, you don't need to create too many consumers, single pattern is recommended.
            var simpleConsumer = await new SimpleConsumer.Builder()
                .SetClientConfig(clientConfig)
                .SetConsumerGroup(consumerGroup)
                .SetAwaitDuration(TimeSpan.FromSeconds(15))
                .SetSubscriptionExpression(subscription)
                .Build();

            while (true)
            {
                var messageViews = await simpleConsumer.Receive(16, TimeSpan.FromSeconds(15));
                foreach (var message in messageViews)
                {
                    logger.LogInformation(
                        $"Received a message, topic={message.Topic}, message-id={message.MessageId}, body-size={message.Body.Length}");
                    await simpleConsumer.Ack(message);
                    logger.LogInformation($"Message is acknowledged successfully, message-id={message.MessageId}");
                    // await simpleConsumer.ChangeInvisibleDuration(message, TimeSpan.FromSeconds(15));
                    // Logger.LogInformation($"Changing message invisible duration successfully, message=id={message.MessageId}");
                }
            }
            // Close the simple consumer if you don't need it anymore.
            // await simpleConsumer.DisposeAsync();
        }
    }
}