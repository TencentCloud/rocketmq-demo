use rocketmq::model::message::MessageBuilder;
use tencent_rocketmq5_demo::util;

#[tokio::main]
async fn main() {
    // 控制台创建的「顺序消息」类型 Topic；消费端的消费组需开启「顺序投递」才能按顺序消费
    let topic = "test-fifo";
    let mut producer = util::new_producer(topic).unwrap();
    let start_result = producer.start().await;
    if start_result.is_err() {
        eprintln!("producer start failure: {:?}", start_result.unwrap_err());
        return;
    }

    // 同一个 message_group 内的消息按发送顺序投递，不同 message_group 之间互不影响
    // 通常用业务主键（如订单号）作为 message_group
    let message_group = "order-001";
    for i in 0..5 {
        let message = MessageBuilder::fifo_message_builder(
            topic,
            format!("hello fifo message {}", i).into_bytes(),
            message_group,
        )
        .set_tag("tagA")
        .set_keys(vec![format!("key-{}", i)])
        .build()
        .unwrap();

        // 顺序消息需要同步发送：上一条发送成功后再发下一条，才能保证顺序
        let send_result = producer.send(message).await;
        if send_result.is_err() {
            eprintln!(
                "send message error, message_group = {}, index = {}: {:?}",
                message_group,
                i,
                send_result.unwrap_err()
            );
            break;
        }
        println!(
            "send message success, message_group = {}, index = {}, id = {}",
            message_group,
            i,
            send_result.unwrap().message_id()
        );
    }

    _ = producer.shutdown().await;
}
