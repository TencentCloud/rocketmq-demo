#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>

using namespace std;
using namespace rocketmq;

/**
 * 同步发送消息
 */
void syncSendMessage();


// 测试main方法
int main() {
    syncSendMessage();
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
    // 请确保参数设置完成在启动之前e
    producer.start();
    return producer;
}


void syncSendMessage() {
    std::cout << "=======Before sending messages=======" << std::endl;
    // 初始化消息生产者
    DefaultMQProducer producer = initProducer();
    // 记录时间
    auto start = std::chrono::system_clock::now();
    int count = 2;
    for (int i = 0; i < count; ++i) {
        // 初始化消息内容
        MQMessage msg(
                "topic",
                "TAG",
                "your keys",
                "Hello cpp client, this is a sync message."  // 消息内容
        );

        try {
            // 发送消息
            SendResult sendResult = producer.send(msg);
            std::cout << "SendResult:" << sendResult.getSendStatus() << ", Message ID: " << sendResult.getMsgId()
                      << std::endl;
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