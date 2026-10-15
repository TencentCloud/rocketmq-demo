using Microsoft.Extensions.Logging;
using Org.Apache.Rocketmq;

namespace examples
{
    internal static class QuickStart
    {
        public static void Main()
        {
           ProducerNormalMessageDemo.QuickStart().Wait();
           SimpleConsumerExample.QuickStart().Wait();
           // Lite Topic 示例
           // ProducerLiteMessageDemo.QuickStart().Wait();
           // LitePushConsumerExample.QuickStart().Wait();
           // 顺序消息示例
           // ProducerFifoMessageDemo.QuickStart().Wait();
           // 事务消息示例
           // ProducerTransactionMessageDemo.QuickStart().Wait();
           // 定时/延时消息示例
           // ProducerDelayMessageDemo.QuickStart().Wait();
           // Tag 过滤示例
           // ProducerTagMessageDemo.QuickStart().Wait();
           // TagPushConsumerExample.QuickStart().Wait();
           // 消费重试示例
           // RetryPushConsumerExample.QuickStart().Wait();
        }
    }
}