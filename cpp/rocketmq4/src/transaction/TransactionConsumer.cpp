#include <iostream>
#include <thread>
#include "include/DefaultMQPushConsumer.h"

using namespace rocketmq;


class ExampleMessageListener : public MessageListenerConcurrently {
public:
    ConsumeStatus consumeMessage(const std::vector<MQMessageExt> &msgs) {
        for (auto item = msgs.begin(); item != msgs.end(); item++) {
            std::cout << "Received Message Topic:" << item->getTopic() << ", MsgId:" << item->getMsgId() << std::endl;
        }
        return CONSUME_SUCCESS;
    }
};

int main(int argc, char *argv[]) {
    std::cout << "=======Before consuming messages=======" << std::endl;
    // 初始化push消费者
    DefaultMQPushConsumer *consumer = new DefaultMQPushConsumer("transaction_group");
    // 设置服务地址
    consumer->setNamesrvAddr("endpoint");
    // 设置用户权限
    consumer->setSessionCredentials(
            "AK",
            "SK",
            "");
    // 设置命名空间
    consumer->setNameSpace("namespace");
    auto start = std::chrono::system_clock::now();



    //register your own listener here to handle the messages received.
    //请注册自定义侦听函数用来处理接收到的消息，并返回响应的处理结果。
    ExampleMessageListener *messageListener = new ExampleMessageListener();
    consumer->subscribe("transaction_topic", "*");
    consumer->registerMessageListener(messageListener);

    //Start this consumer
    //准备工作完成，必须调用启动函数，才可以正常工作。
    // ********************************************
    // 1.确保订阅关系的设置在启动之前完成。
    // 2.确保相同GID下面的消费者的订阅关系一致。
    // *********************************************
    consumer->start();

    //Keep main thread running until process finished.
    //请保持线程常驻，不要执行shutdown操作。
    std::this_thread::sleep_for(std::chrono::milliseconds(60 * 1000));
    consumer->shutdown();
    std::cout << "=======After consuming messages======" << std::endl;
    return 0;
}