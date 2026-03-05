use std::{
    ops::Add,
    time::{Duration, SystemTime, UNIX_EPOCH},
};

use rocketmq::model::message::MessageBuilder;

use tencent_rocketmq5_demo::util;

#[tokio::main]
async fn main() {
    let topic = "test-delay";
    let mut producer = util::new_producer(topic).unwrap();
    let start_result = producer.start().await;
    if start_result.is_err() {
        eprintln!("producer start failure: {:?}", start_result.unwrap_err());
        return;
    }

    let message = MessageBuilder::delay_message_builder(
        topic,
        "hello delay message".as_bytes().to_vec(),
        SystemTime::now()
            .add(Duration::from_secs(600))
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_secs() as i64,
    )
    .set_tag("tagA")
    .set_keys(vec!["keyA"])
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
