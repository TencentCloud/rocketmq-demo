use rocketmq::model::{
    message::MessageBuilder,
    transaction::{Transaction, TransactionResolution},
};
use tencent_rocketmq5_demo::util;

#[tokio::main]
async fn main() {
    // 创建生产者
    let topic = "test";
    let transaction_checker = Box::new(|transaction_id, message_id| {
        // MQ回查本地事务状态,通常是 commit 没有执行后一段时间 MQ 会回查,返回 COMMIT 表示事务成功,返回其他表示失败
        // 可根据transaction_id 和 message_id 反查业务系统
        println!(
            "check transaction status, transaction={:?}, message={:?}.",
            transaction_id, message_id
        );
        TransactionResolution::COMMIT
    });
    let mut producer = util::new_transaction_producer(topic, transaction_checker).unwrap();
    let _ = producer.start().await;

    // 发送事务消息
    let body = "it's a transaction message".as_bytes().to_vec();
    let message = MessageBuilder::transaction_message_builder(topic, body)
        .set_tag("tag1")
        .build()
        .unwrap();
    let transaction = producer.send_transaction_message(message).await.unwrap();

    let message_id = String::from(transaction.message_id());
    let transaction_id = String::from(transaction.transaction_id());
    // 处理业务逻辑
    // 处理完成后
    // 如果是业务处理成功,调用 COMMIT告知 MQ 可以投递消息给下游消费者
    let result = transaction.commit().await;
    if result.is_err() {
        eprintln!(
            "transaction {} commit message {} exception.",
            transaction_id, message_id
        );
    } else {
        println!(
            "transaction={}, message_id={}, completes!",
            transaction_id, message_id
        );
    }
    //
    let _ = producer.shutdown().await;
}
