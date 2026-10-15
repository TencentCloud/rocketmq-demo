import { ConsumeResult, PushConsumer } from 'rocketmq-client-nodejs';

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的消费组
const consumerGroup = 'yourConsumerGroup';
// 控制台创建的「普通消息」类型的 Topic 名称
const topic = 'yourNormalTopic';

const consumer = new PushConsumer({
  endpoints,
  sessionCredentials,
  consumerGroup,
  // 订阅关系：Map<topic, tag 过滤表达式>，'*' 表示订阅全部 tag
  subscriptions: new Map([[ topic, '*' ]]),
  messageListener: {
    async consume(messageView) {
      console.log('consume message, topic=%s, tag=%s, messageId=%s, body=%s',
        messageView.topic, messageView.tag, messageView.messageId, messageView.body.toString());
      // 消费成功返回 SUCCESS；返回 FAILURE 或抛出异常时消息会被重新投递
      return ConsumeResult.SUCCESS;
    },
  },
});
// startup() 失败会直接抛出异常并退出进程
await consumer.startup();
console.log('push consumer started, consumerGroup=%s, topic=%s', consumerGroup, topic);

// Ctrl+C 退出时关闭 consumer
process.once('SIGINT', async () => {
  await consumer.shutdown();
  process.exit(0);
});
