import { Producer } from 'rocketmq-client-nodejs';

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的「普通消息」类型的 Topic 名称
const topic = 'yourNormalTopic';
// 发送三种 tag 的消息，消费者可以按 tag 过滤
const tags = [ 'TagA', 'TagB', 'TagC' ];

const producer = new Producer({
  endpoints,
  sessionCredentials,
});
await producer.startup();

try {
  for (let i = 0; i < 9; i++) {
    const tag = tags[i % tags.length];
    try {
      const receipt = await producer.send({
        topic,
        // 消息标签（tag），Topic 下的二级分类，不能包含 '|'
        tag,
        keys: [ `yourMessageKey-${i}` ],
        body: Buffer.from(`This is a tag message, tag=${tag}`),
      });
      console.log('send message success, tag=%s, messageId=%s', tag, receipt.messageId);
    } catch (error) {
      console.error('send message failed, tag=%s', tag, error);
    }
  }
} finally {
  // 不再使用时关闭 producer
  await producer.shutdown();
}
