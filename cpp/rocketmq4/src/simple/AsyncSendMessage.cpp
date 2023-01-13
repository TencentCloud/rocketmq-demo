#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>
#include <vector>

using namespace std;
using namespace rocketmq;


/**
 * 异步发送消息
 */
void asyncSendMessage();

// 测试main方法
int main() {
    asyncSendMessage();
}


/**
 * 初始化消息生产者
 * @return 消息生产者
 */
DefaultMQProducer initProducer() {
    // 初始化消息生产者，并设置生产者组名称
    DefaultMQProducer producer("group");
    // 设置服务地址
    producer.setNamesrvAddr("endpoint");
    // 设置用户权限
    producer.setSessionCredentials(
            "AK",
            "SK",
            "");
    // 设置命名空间
    producer.setNameSpace("namespace");
    // 请确保参数设置完成在启动之前
    producer.start();
    return producer;
}


// 异步回调
class MySendCallback : public SendCallback {
    virtual void onSuccess(SendResult &sendResult) {
        std::cout << "SendResult:" << sendResult.getSendStatus() << ", Message ID: " << sendResult.getMsgId()
                  << std::endl;
    }

    virtual void onException(MQException &e) { cout << "send Exception\n"; }
};

void asyncSendMessage() {
    std::cout << "=======Before sending messages=======" << std::endl;
    // 初始化消息生产者
    DefaultMQProducer producer = initProducer();
    // 记录时间
    auto start = std::chrono::system_clock::now();
    int count = 10;
    for (int i = 0; i < count; ++i) {
        // topic名称及消息tag设置
        MQMessage msg("topic",
                      "your tags",
                      "your keys",
                      "Hello cpp client, this is a async message."
        );

        try {
            // 异步发送消息
            producer.send(msg, new MySendCallback());
            this_thread::sleep_for(chrono::seconds(1));
        } catch (MQException e) {
            std::cout << "ErrorCode: " << e.GetError() << " Exception:" << e.what() << std::endl;
        }
    }
    auto interval = std::chrono::system_clock::now() - start;
    // 打印发送结果
    std::cout << "Send " << count << " messages OK, costs "
              << std::chrono::duration_cast<std::chrono::milliseconds>(interval).count() << "ms" << std::endl;
    // 释放资源
    producer.shutdown();
    std::cout << "=======After sending messages=======" << std::endl;
}