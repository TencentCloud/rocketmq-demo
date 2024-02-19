use rocketmq::model::common::{FilterExpression, FilterType};

use tencent_rocketmq5_demo::util;

#[tokio::main]
async fn main() {
    let topic = "test-transaction";
    let group = "test";
    let mut consumer = util::new_simple_consumer(topic, group).unwrap();
    let start_result = consumer.start().await;
    if start_result.is_err() {
        eprintln!("consumer start failed: {:?}", start_result.unwrap_err());
        return;
    }

    let mut i = 10;
    let filter_exp = FilterExpression::new(FilterType::Tag, "tag1");
    while i > 0 {
        let receive_result = consumer.receive(topic, &filter_exp).await;
        if receive_result.is_err() {
            eprintln!("receive message failed: {:?}", receive_result.unwrap_err());
            break;
        }

        let messages = receive_result.unwrap();

        if messages.is_empty() {
            println!("no messages");
            i = i - 1;
            continue;
        }

        for message in messages {
            println!("receive message : {:?}", message);
            // 业务处理逻辑，处理完就ACK
            let ack_result = consumer.ack(&message).await;
            if ack_result.is_err() {
                eprintln!("ack message failed: {:?}", ack_result.unwrap_err())
            }
        }

        i = i - 1;
    }
    _ = consumer.shutdown().await;
}
