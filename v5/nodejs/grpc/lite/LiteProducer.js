import { Producer, LiteTopicQuotaExceededException } from 'rocketmq-client-nodejs';

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的「轻量消息」类型的一级 Topic 名称
const parentTopic = 'yourParentTopic';

const producer = new Producer({
  endpoints,
  sessionCredentials,
});
await producer.startup();

try {
  for (let i = 0; i < 10; i++) {
    // 每条消息可以指定不同的 lite topic（二级 Topic），无需提前在控制台创建
    const liteTopic = `lite-topic-${i % 3}`;
    try {
      const receipt = await producer.send({
        topic: parentTopic,
        keys: [ `yourMessageKey-${i}` ],
        body: Buffer.from(`This is a lite message, liteTopic=${liteTopic}`),
        // 设置 lite topic。注意：不能与 messageGroup、deliveryTimestamp、priority 同时使用
        liteTopic,
      });
      console.log('send message success, liteTopic=%s, messageId=%s', liteTopic, receipt.messageId);
    } catch (error) {
      if (error instanceof LiteTopicQuotaExceededException) {
        // lite topic 数量超过配额，需要评估并提升 lite topic 资源上限
        console.error('lite topic quota exceeded, liteTopic=%s', liteTopic, error);
      } else {
        console.error('send message failed, liteTopic=%s', liteTopic, error);
      }
    }
  }
} finally {
  // 不再使用时关闭 producer
  await producer.shutdown();
}
