use rocketmq::conf::ClientOption;
use rocketmq::error::ClientError;

// 服务端返回的 lite topic 配额错误码
const LITE_TOPIC_QUOTA_EXCEEDED: &str = "42901";
const LITE_SUBSCRIPTION_QUOTA_EXCEEDED: &str = "42902";

pub fn common_client_option() -> ClientOption {
    let mut client_option = ClientOption::default();
    // 填入接入点、AccessKey、SecretKey
    // TODO: 添加实例的信息
    client_option.set_access_url("rmq-xxxx.....:8080");
    client_option.set_access_key("ak....");
    client_option.set_secret_key("sk....");
    client_option
}

fn has_code(error: &ClientError, code: &str) -> bool {
    error
        .context()
        .iter()
        .any(|(key, value)| *key == "code" && value == code)
}

/// 发送时 lite topic 数量超过配额
pub fn is_lite_topic_quota_exceeded(error: &ClientError) -> bool {
    has_code(error, LITE_TOPIC_QUOTA_EXCEEDED)
}

/// 订阅时 lite topic 订阅数超过配额
pub fn is_lite_subscription_quota_exceeded(error: &ClientError) -> bool {
    has_code(error, LITE_SUBSCRIPTION_QUOTA_EXCEEDED)
}
