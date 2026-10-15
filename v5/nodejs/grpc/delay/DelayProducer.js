import { Producer } from 'rocketmq-client-nodejs';

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的「定时/延时消息」类型的 Topic 名称
const delayTopic = 'yourDelayTopic';
// 延迟时间（毫秒）
const delayMillis = 10000;

const producer = new Producer({
  endpoints,
  sessionCredentials,
});
await producer.startup();

try {
  for (let i = 0; i < 10; i++) {
    // 期望投递时间：当前时间 + 延迟时间；定时消息直接指定一个未来的时间点即可
    const deliveryTimestamp = new Date(Date.now() + delayMillis);
    try {
      const receipt = await producer.send({
        topic: delayTopic,
        tag: 'TagA',
        keys: [ `yourMessageKey-${i}` ],
        body: Buffer.from(`This is a delay message, index=${i}`),
        // 设置投递时间。注意：不能与 messageGroup、liteTopic、priority 同时使用
        deliveryTimestamp,
      });
      console.log('send message success, deliveryTimestamp=%s, messageId=%s',
        deliveryTimestamp.toISOString(), receipt.messageId);
    } catch (error) {
      console.error('send message failed, index=%d', i, error);
    }
  }
} finally {
  // 不再使用时关闭 producer
  await producer.shutdown();
}
