use std::time::Duration;

use rocketmq::model::common::{FilterExpression, FilterType};

use tencent_rocketmq5_demo::util;

#[tokio::main]
async fn main() {
    // 控制台创建的 Topic 和消费组
    // 最大重试次数由消费组的重试策略决定（控制台消费组配置），超过后消息进入死信，不再投递
    let topic = "test";
    let group = "test";
    let mut consumer = util::new_simple_consumer(topic, group).unwrap();
    let start_result = consumer.start().await;
    if start_result.is_err() {
        eprintln!("consumer start failed: {:?}", start_result.unwrap_err());
        return;
    }

    // 不可见时间：消息被接收后在这段时间内不会再投递给任何消费者；
    // 不 ack 的消息在不可见时间到期后会被重新投递，delivery_attempt 加 1
    let invisible_duration = Duration::from_secs(10);
    // 演示用：前几次投递不 ack（模拟消费失败），第 ACK_ON_ATTEMPT 次投递时 ack 并结束演示
    const ACK_ON_ATTEMPT: i32 = 3;
    let filter_exp = FilterExpression::new(FilterType::Tag, "*");

    let mut empty_rounds = 10;
    let mut finished = false;
    while !finished && empty_rounds > 0 {
        let receive_result = consumer
            .receive_with(topic, &filter_exp, 1, invisible_duration)
            .await;
        if receive_result.is_err() {
            eprintln!("receive message failed: {:?}", receive_result.unwrap_err());
            break;
        }

        let messages = receive_result.unwrap();
        if messages.is_empty() {
            println!("no messages");
            empty_rounds -= 1;
            continue;
        }

        for message in messages {
            // delivery_attempt 表示第几次投递，首次投递为 1
            let delivery_attempt = message.delivery_attempt();
            if delivery_attempt < ACK_ON_ATTEMPT {
                // 不 ack：消息会在不可见时间到期后重新投递
                println!(
                    "consume failed, not ack, id = {}, delivery_attempt = {}, will be redelivered after {:?}",
                    message.message_id(),
                    delivery_attempt,
                    invisible_duration
                );
                continue;
            }

            let ack_result = consumer.ack(&message).await;
            if ack_result.is_err() {
                eprintln!(
                    "ack message failed, id = {}, delivery_attempt = {}: {:?}",
                    message.message_id(),
                    delivery_attempt,
                    ack_result.unwrap_err()
                );
                continue;
            }
            println!(
                "consume success, ack message, id = {}, delivery_attempt = {}",
                message.message_id(),
                delivery_attempt
            );
            finished = true;
        }
    }
    if let Err(e) = consumer.shutdown().await {
        eprintln!("consumer shutdown failed: {:?}", e);
    }
}
