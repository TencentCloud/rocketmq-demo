use rocketmq::conf::PushConsumerOption;
use rocketmq::model::message::MessageView;
use rocketmq::{
    ConsumeResult, LitePushConsumer, LitePushConsumerTrait, OffsetOption, OffsetPolicy,
};
use tencent_rocketmq5_lite_demo::util::{
    common_client_option, is_lite_subscription_quota_exceeded,
};

#[tokio::main]
async fn main() {
    // 控制台创建的消费组，需选择「Lite Topic 消费」模式并绑定到下面的一级 Topic
    let consumer_group = "yourConsumerGroup";
    // 控制台创建的「轻量消息」类型的一级 Topic 名称
    let bind_topic = "yourParentTopic";

    let mut option = PushConsumerOption::default();
    option.set_consumer_group(consumer_group);

    let message_listener = Box::new(|message: &MessageView| {
        println!(
            "consume message, liteTopic={:?}, messageId={}, body={}",
            message.lite_topic(),
            message.message_id(),
            String::from_utf8_lossy(message.body())
        );
        // 消费成功返回 SUCCESS，失败返回 FAILURE 后消息会被重新投递
        ConsumeResult::SUCCESS
    });

    // In most case, you don't need to create too many consumers, singleton pattern is recommended.
    // 绑定一级 Topic，后续订阅的 lite topic 都属于该 Topic
    let mut consumer = LitePushConsumer::new(
        common_client_option(),
        option,
        bind_topic.to_string(),
        message_listener,
    )
    .unwrap();
    if let Err(e) = consumer.start().await {
        eprintln!("lite push consumer start failed: {:?}", e);
        return;
    }

    // subscribe_lite 会发起网络请求并校验配额，可能失败，需要检查结果：
    // 1. 网络请求失败，可以稍后重试；
    // 2. 配额校验失败，需要评估配额是否足够，并及时通过 unsubscribe_lite 取消不再使用的订阅以释放配额。
    let subscriptions = vec![
        // 从上次消费位点开始消费（默认）
        (
            "lite-topic-0",
            OffsetOption::from_policy(OffsetPolicy::Last),
        ),
        // 从最早位点开始消费
        ("lite-topic-1", OffsetOption::from_policy(OffsetPolicy::Min)),
        // 从最后 100 条消息开始消费
        ("lite-topic-2", OffsetOption::from_tail_n(100)),
    ];
    for (lite_topic, offset_option) in subscriptions {
        match consumer
            .subscribe_lite_with_offset(lite_topic.to_string(), offset_option)
            .await
        {
            Ok(_) => println!("subscribed lite topic: {}", lite_topic),
            Err(e) if is_lite_subscription_quota_exceeded(&e) => {
                // 评估并提升 lite topic 订阅配额，并及时 unsubscribe_lite 不再使用的订阅
                eprintln!(
                    "lite subscription quota exceeded, liteTopic={}: {:?}",
                    lite_topic, e
                )
            }
            // 稍后重试
            Err(e) => eprintln!(
                "subscribe lite topic failed, liteTopic={}: {:?}",
                lite_topic, e
            ),
        }
    }
    println!(
        "subscribed lite topics: {:?}",
        consumer.get_lite_topic_set()
    );

    // 不再需要时取消订阅，释放配额
    // consumer.unsubscribe_lite("lite-topic-2".to_string()).await.unwrap();

    // Ctrl+C 退出
    tokio::signal::ctrl_c().await.unwrap();
    if let Err(e) = consumer.shutdown().await {
        eprintln!("lite push consumer shutdown failed: {:?}", e);
    }
}
