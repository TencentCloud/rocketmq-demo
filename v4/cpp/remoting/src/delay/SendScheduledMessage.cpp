#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>

using namespace std;
using namespace rocketmq;


/**
 * 发送定时消息
 */
void sendScheduledMessage();

int main() {
    sendScheduledMessage();
}

/**
 * 初始化并启动消息生产者
 * DefaultMQProducer 内部持有裸指针且未禁用拷贝，不要按值返回或拷贝，这里通过引用传入调用方创建的对象
 * @param producer 消息生产者
 */
void initProducer(DefaultMQProducer &producer) {
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
}


void sendScheduledMessage() {
    std::cout << "=======Before sending messages=======" << std::endl;
    // 初始化消息生产者，并设置生产者组名称
    DefaultMQProducer producer("gtimer");
    initProducer(producer);
    // 记录时间
    auto start = std::chrono::system_clock::now();
    int count = 10;
    for (int i = 0; i < count; ++i) {
        // topic名称及消息tag设置
        MQMessage msg("timer",
                      "your tags",
                      "your keys",
                      "Hello cpp client, this is a sync message."
        );
        // 获取时间戳
        long time = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().operator+=(
                std::chrono::milliseconds(30000)).time_since_epoch()).count();
        // 设置消息发送的具体时间
        msg.setProperty("__STARTDELIVERTIME", to_string(time));
        try {
            // 发送消息
            SendResult sendResult = producer.send(msg);
            std::cout << "SendResult:" << sendResult.getSendStatus() << ", Message ID: " << sendResult.getMsgId()
                      << std::endl;
        } catch (MQException &e) {
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