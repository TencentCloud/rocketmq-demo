import { ConsumeResult, PushConsumer } from 'rocketmq-client-nodejs';

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的消费组。最大重试次数由消费组的重试策略决定（控制台消费组配置），
// 超过最大重试次数仍消费失败的消息会进入死信队列
const consumerGroup = 'yourConsumerGroup';
// 控制台创建的「普通消息」类型的 Topic 名称
const topic = 'yourNormalTopic';
// 示例：前两次投递返回 FAILURE，第三次投递时返回 SUCCESS，用来观察重试过程
const succeedAtAttempt = 3;

const consumer = new PushConsumer({
  endpoints,
  sessionCredentials,
  consumerGroup,
  subscriptions: new Map([[ topic, '*' ]]),
  messageListener: {
    async consume(messageView) {
      // deliveryAttempt 表示这是该消息第几次投递，首次投递为 1
      const deliveryAttempt = messageView.deliveryAttempt;
      console.log('consume message, messageId=%s, deliveryAttempt=%s, body=%s',
        messageView.messageId, deliveryAttempt, messageView.body.toString());
      if (deliveryAttempt === undefined || deliveryAttempt < succeedAtAttempt) {
        // 返回 FAILURE（或在这里抛出异常）会触发消费重试，消息在退避时间后重新投递
        console.log('consume failed, wait for retry, messageId=%s', messageView.messageId);
        return ConsumeResult.FAILURE;
      }
      console.log('consume success after retry, messageId=%s', messageView.messageId);
      return ConsumeResult.SUCCESS;
    },
  },
});
// 注意：生产者的 maxAttempts 是发送失败时的重试次数，与这里的消费重试无关
// startup() 失败会直接抛出异常并退出进程
await consumer.startup();
console.log('retry push consumer started, consumerGroup=%s, topic=%s', consumerGroup, topic);

// Ctrl+C 退出时关闭 consumer
process.once('SIGINT', async () => {
  await consumer.shutdown();
  process.exit(0);
});
