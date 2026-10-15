import {
  LiteSimpleConsumerBuilder,
  LiteSubscriptionQuotaExceededException,
  OffsetOption,
} from 'rocketmq-client-nodejs';

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的消费组，需选择「Lite Topic 消费」模式并绑定到下面的一级 Topic
const consumerGroup = 'yourConsumerGroup';
// 控制台创建的「轻量消息」类型的一级 Topic 名称
const parentTopic = 'yourParentTopic';
// 消息被接收后的不可见时间（毫秒），超时未 ack 的消息会重新投递
const invisibleDuration = 15000;

// build() 会同时启动 consumer
const consumer = await new LiteSimpleConsumerBuilder()
  .setClientConfiguration({ endpoints, sessionCredentials })
  .setConsumerGroup(consumerGroup)
  // 绑定一级 Topic，后续订阅的 lite topic 都属于该 Topic
  .bindTopic(parentTopic)
  // receive() 长轮询的等待时间（毫秒）
  .setAwaitDuration(5000)
  .build();

try {
  // subscribeLite() 会发起网络请求并校验配额，可能失败，需要检查结果
  await consumer.subscribeLite('lite-topic-0');
  await consumer.subscribeLite('lite-topic-1', OffsetOption.MIN_OFFSET);
  console.log('subscribed lite topics: %o', [ ...consumer.getLiteTopicSet() ]);
} catch (error) {
  if (error instanceof LiteSubscriptionQuotaExceededException) {
    // 评估并提升 lite topic 订阅配额，并及时 unsubscribeLite() 不再使用的订阅
    console.error('lite subscription quota exceeded', error);
  } else {
    // 稍后重试
    console.error('subscribe lite topic failed', error);
  }
}

let running = true;
// Ctrl+C 退出循环并关闭 consumer
process.once('SIGINT', () => {
  running = false;
});

try {
  while (running) {
    try {
      const messages = await consumer.receive(16, invisibleDuration);
      for (const message of messages) {
        console.log('receive message, liteTopic=%s, messageId=%s, body=%s',
          message.liteTopic, message.messageId, message.body.toString());
        // 业务处理成功后 ack，否则消息会在不可见时间结束后重新投递
        await consumer.ack(message);
      }
    } catch (error) {
      console.error('receive or ack message failed', error);
    }
  }
} finally {
  await consumer.close();
}
