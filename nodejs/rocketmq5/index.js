import { Producer,SimpleConsumer } from 'rocketmq-client-nodejs';

const producer = new Producer({
  endpoints: 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080',
  sessionCredentials: {
    accessKey: 'yourAccessKey',
    secretKey: 'yourSecretKey',
  }
});
await producer.startup();

const receipt = await producer.send({
  topic: 'TopicTest',
  body: Buffer.from(JSON.stringify({
    hello: 'rocketmq-client-nodejs world 😄',
    now: Date(),
  })),
});
console.log(receipt);


const simpleConsumer = new SimpleConsumer({
    endpoints: 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080',
  sessionCredentials: {
    accessKey: 'yourAccessKey',
    secretKey: 'yourSecretKey',
  },
  consumerGroup: 'nodejs-demo-group',
  subscriptions: new Map().set('TopicTest', '*'),
});
await simpleConsumer.startup();

const messages = await simpleConsumer.receive(20);
console.log('got %d messages', messages.length);
for (const message of messages) {
  console.log(message);
  console.log('body=%o', message.body.toString());
  await simpleConsumer.ack(message);
}