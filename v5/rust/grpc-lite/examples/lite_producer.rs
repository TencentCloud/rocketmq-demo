use rocketmq::conf::ProducerOption;
use rocketmq::model::message::MessageBuilder;
use rocketmq::Producer;
use tencent_rocketmq5_lite_demo::util::{common_client_option, is_lite_topic_quota_exceeded};

#[tokio::main]
async fn main() {
    // 控制台创建的「轻量消息」类型的一级 Topic 名称
    let topic = "yourParentTopic";

    let mut producer_option = ProducerOption::default();
    // 设置主题名，用于提前获取路由
    producer_option.set_topics(vec![topic]);

    // In most case, you don't need to create too many producers, singleton pattern is recommended.
    let mut producer = Producer::new(producer_option, common_client_option()).unwrap();
    if let Err(e) = producer.start().await {
        eprintln!("producer start failed: {:?}", e);
        return;
    }

    for i in 0..10 {
        // 每条消息可以指定不同的 lite topic（二级 Topic），无需提前在控制台创建
        let lite_topic = format!("lite-topic-{}", i % 3);
        let body = format!("This is a lite message, liteTopic={}", lite_topic);
        // 注意：lite topic 不能与 message_group、delivery_timestamp、priority 同时使用
        let message = MessageBuilder::lite_message_builder(topic, body.into_bytes(), &lite_topic)
            .set_keys(vec![format!("yourMessageKey-{}", i)])
            .build()
            .unwrap();

        match producer.send(message).await {
            Ok(receipt) => println!(
                "send message success, liteTopic={}, messageId={}",
                lite_topic,
                receipt.message_id()
            ),
            Err(e) if is_lite_topic_quota_exceeded(&e) => {
                // lite topic 数量超过配额，需要评估并提升 lite topic 资源上限
                eprintln!(
                    "lite topic quota exceeded, liteTopic={}: {:?}",
                    lite_topic, e
                )
            }
            Err(e) => eprintln!("send message failed, liteTopic={}: {:?}", lite_topic, e),
        }
    }

    // 不再使用时关闭 producer
    if let Err(e) = producer.shutdown().await {
        eprintln!("producer shutdown failed: {:?}", e);
    }
}
