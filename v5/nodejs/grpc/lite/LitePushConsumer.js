import {
  ConsumeResult,
  LitePushConsumerBuilder,
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

// build() 会同时启动 consumer
const consumer = await new LitePushConsumerBuilder()
  .setClientConfiguration({ endpoints, sessionCredentials })
  .setConsumerGroup(consumerGroup)
  // 绑定一级 Topic，后续订阅的 lite topic 都属于该 Topic
  .bindTopic(parentTopic)
  .setMessageListener({
    async consume(messageView) {
      console.log('consume message, liteTopic=%s, messageId=%s, body=%s',
        messageView.liteTopic, messageView.messageId, messageView.body.toString());
      // 消费成功返回 SUCCESS，失败返回 FAILURE 后消息会被重新投递
      return ConsumeResult.SUCCESS;
    },
  })
  .build();

try {
  // subscribeLite() 会发起网络请求并校验配额，可能失败，需要检查结果：
  // 1. 网络请求失败，可以稍后重试；
  // 2. 配额校验失败（LiteSubscriptionQuotaExceededException），需要评估配额是否足够，
  //    并及时通过 unsubscribeLite() 取消不再使用的订阅以释放配额。
  // 默认从最新位点开始消费
  await consumer.subscribeLite('lite-topic-0');
  // 从最早位点开始消费
  await consumer.subscribeLite('lite-topic-1', OffsetOption.MIN_OFFSET);
  // 从最后 100 条消息开始消费
  await consumer.subscribeLite('lite-topic-2', OffsetOption.ofTailN(100));
  console.log('subscribed lite topics: %o', [ ...consumer.getLiteTopicSet() ]);
} catch (error) {
  if (error instanceof LiteSubscriptionQuotaExceededException) {
    // 1. 评估并提升 lite topic 订阅配额
    // 2. 及时取消不再使用的订阅，例如：await consumer.unsubscribeLite('lite-topic-2');
    console.error('lite subscription quota exceeded', error);
  } else {
    // 稍后重试
    console.error('subscribe lite topic failed', error);
  }
}

// Ctrl+C 退出时关闭 consumer
process.once('SIGINT', async () => {
  await consumer.close();
  process.exit(0);
});
