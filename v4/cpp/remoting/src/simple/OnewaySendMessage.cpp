#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>
#include <vector>

using namespace std;
using namespace rocketmq;


/**
 * 单向发送消息
 */
void onewaySendMessage();


// 测试main方法
int main() {
    onewaySendMessage();
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


void onewaySendMessage() {
    std::cout << "=======Before sending messages=======" << std::endl;
    // 初始化消息生产者，并设置生产者组名称
    DefaultMQProducer producer("group");
    initProducer(producer);
    // 记录时间
    auto start = std::chrono::system_clock::now();
    int count = 10;
    for (int i = 0; i < count; ++i) {
        // topic名称及消息tag设置
        MQMessage msg("topic",
                      "your tags",
                      "your keys",
                      "Hello cpp client, this is a oneway message."
        );
        try {
            // 单向发送消息
            producer.sendOneway(msg);
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