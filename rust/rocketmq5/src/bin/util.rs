use rocketmq::conf::{ClientOption, ProducerOption, SimpleConsumerOption};
use rocketmq::error::ClientError;
use rocketmq::{Producer, SimpleConsumer};


fn common_client_option() -> ClientOption {
    let mut client_option = ClientOption::default();
    // 填入接入点、AccessKey、SecretKey
    // TODO: 添加实例的信息
    client_option.set_access_url("rmq-xxxx....:8080");
    client_option.set_access_key("ak....");
    client_option.set_secret_key("sk....");
    client_option
}

#[allow(dead_code)]
pub fn new_producer(topic: impl Into<String>) -> Result<Producer, ClientError> {
    let mut option = ProducerOption::default();
    // 设置主题名
    option.set_topics(vec![topic]);

    Producer::new(option, common_client_option())
}

#[allow(dead_code)]
pub fn new_simple_consumer(topic: impl Into<String>, group: impl Into<String>) -> Result<SimpleConsumer, ClientError>{
    let mut consumer_option = SimpleConsumerOption::default();
    consumer_option.set_consumer_group(group);
    consumer_option.set_topics(vec![topic]);

    SimpleConsumer::new(consumer_option, common_client_option())
}