import { Producer } from 'rocketmq-client-nodejs';

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的「顺序消息」类型的 Topic 名称；消费组需开启顺序投递才能按顺序消费
const fifoTopic = 'yourFifoTopic';

const producer = new Producer({
  endpoints,
  sessionCredentials,
});
await producer.startup();

try {
  for (let i = 0; i < 10; i++) {
    // 同一个 messageGroup 内的消息按发送顺序投递，不同 messageGroup 之间互不影响
    const messageGroup = `yourMessageGroup-${i % 2}`;
    try {
      const receipt = await producer.send({
        topic: fifoTopic,
        tag: 'TagA',
        keys: [ `yourMessageKey-${i}` ],
        body: Buffer.from(`This is a fifo message, index=${i}`),
        // 设置消息组。注意：不能与 deliveryTimestamp、liteTopic、priority 同时使用
        messageGroup,
      });
      console.log('send message success, messageGroup=%s, messageId=%s', messageGroup, receipt.messageId);
    } catch (error) {
      console.error('send message failed, messageGroup=%s', messageGroup, error);
    }
  }
} finally {
  // 不再使用时关闭 producer
  await producer.shutdown();
}
