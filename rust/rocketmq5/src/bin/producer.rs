use rocketmq::model::message::MessageBuilder;
use tencent_rocketmq5_demo::util;

#[tokio::main]
async fn main() {
    let topic = "test";
    let mut producer = util::new_producer(topic).unwrap();
    let start_result = producer.start().await;
    if start_result.is_err() {
        eprintln!("producer start failure: {:?}", start_result.unwrap_err());
        return;
    }

    let message = MessageBuilder::builder()
        .set_topic(topic)
        .set_tag("tag1")
        .set_body("hello world".as_bytes().to_vec())
        .build()
        .unwrap();

    let send_result = producer.send(message).await;
    if send_result.is_err() {
        eprintln!("send message error: {:?}", send_result.unwrap_err());
        return;
    }
    println!(
        "send message success, id = {}",
        send_result.unwrap().message_id()
    );

    _ = producer.shutdown().await;
}
