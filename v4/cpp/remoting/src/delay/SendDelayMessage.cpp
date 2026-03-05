#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>

using namespace std;
using namespace rocketmq;

/**
 * 发送延迟消息
 */
void sendDelayMessage();

int main() {
    sendDelayMessage();
}

/**
 * 初始化消息生产者
 * @return 消息生产者
 */
DefaultMQProducer initProducer() {
    // group名称
    DefaultMQProducer producer("gtimer");
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


void sendDelayMessage() {
    std::cout << "=======Before sending messages=======" << std::endl;
    // 初始化消息生产者
    DefaultMQProducer producer = initProducer();
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
        // 设置延迟级别
        // 等级与时间对应关系：
        // 1s、 5s、 10s、 30s、 1m、 2m、 3m、 4m、 5m、 6m、 7m、 8m、 9m、 10m、 20m、 30m、 1h、 2h；
        // 1    2    3     4     5    6   7    8   9    10   11   12  13   14    15    16   17   18
        msg.setDelayTimeLevel(3);
        try {
            // 发送消息
            SendResult sendResult = producer.send(msg);
            std::cout << "SendResult:" << sendResult.getSendStatus() << ", Message ID: " << sendResult.getMsgId()
                      << std::endl;
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