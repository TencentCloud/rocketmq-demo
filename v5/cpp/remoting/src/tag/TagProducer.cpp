#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>

using namespace std;
using namespace rocketmq;

/**
 * 发送带Tag的消息
 */
int sendTagMessage();


// 测试main方法
int main() {
    return sendTagMessage();
}


int sendTagMessage() {
    std::cout << "=======Before sending messages=======" << std::endl;
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
    try {
        // 请确保参数设置完成在启动之前
        producer.start();
    } catch (MQException &e) {
        std::cout << "Start producer failed, ErrorCode: " << e.GetError() << " Exception:" << e.what() << std::endl;
        return -1;
    }

    // 依次发送TagA、TagB、TagC三种Tag的消息，消费者订阅 "TagA || TagB" 时只会收到TagA和TagB的消息
    const std::string tags[] = {"TagA", "TagB", "TagC"};
    int count = 6;
    for (int i = 0; i < count; ++i) {
        const std::string &tag = tags[i % 3];
        // 初始化消息内容（topic名称、tag、key、消息内容）
        MQMessage msg(
                "topic",
                tag,
                "your keys",
                "Hello cpp client, this is a tag message, tag=" + tag  // 消息内容
        );

        try {
            // 发送消息
            SendResult sendResult = producer.send(msg);
            std::cout << "SendResult:" << sendResult.getSendStatus() << ", Message ID: " << sendResult.getMsgId()
                      << ", TAG: " << tag << std::endl;
            this_thread::sleep_for(chrono::seconds(1));
        } catch (MQException &e) {
            std::cout << "ErrorCode: " << e.GetError() << " Exception:" << e.what() << std::endl;
        }
    }

    // 释放资源
    producer.shutdown();
    std::cout << "=======After sending messages=======" << std::endl;
    return 0;
}
