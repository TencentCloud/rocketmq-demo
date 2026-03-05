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
        }
    }
}