#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>
#include <vector>

using namespace std;
using namespace rocketmq;

/**
 * 批量发送方式，生产者客户端批量发送存在问题，暂时不建议使用
 */

/**
 * 批量发送消息
 */
void batchSendMessage();

// 测试main方法
int main() {
    batchSendMessage();
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

void batchSendMessage() {
    std::cout << "=======Before sending messages=======" << std::endl;
    // 初始化消息生产者
    DefaultMQProducer producer = initProducer();
    // 记录时间
    auto start = std::chrono::system_clock::now();

    vector<MQMessage> msgs;
    MQMessage msg1("topic", "*", "Hello cpp client, this is a batch message.");
    MQMessage msg2("topic", "*", "Hello cpp client, this is a batch message.");
    MQMessage msg3("topic", "*", "Hello cpp client, this is a batch message.");
    msgs.push_back(msg1);
    msgs.push_back(msg2);
    msgs.push_back(msg3);

    try {
        // 批量发送消息
        SendResult sendResult = producer.send(msgs);
    } catch (MQException e) {
        std::cout << "ErrorCode: " << e.GetError() << " Exception:" << e.what() << std::endl;
    }
    auto interval = std::chrono::system_clock::now() - start;
    // 打印发送结果
    std::cout << "Send " << " messages OK, costs "
              << std::chrono::duration_cast<std::chrono::milliseconds>(interval).count() << "ms" << std::endl;
    // 释放资源
    producer.shutdown();
    std::cout << "=======After sending messages=======" << std::endl;
}