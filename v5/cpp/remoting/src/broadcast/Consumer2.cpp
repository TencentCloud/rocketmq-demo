#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>
#include <include/MQMessageListener.h>
#include <include/DefaultMQPushConsumer.h>


using namespace std;
using namespace rocketmq;

int simplePushConsumer();

int main() {
    simplePushConsumer();
}

// 消息监听
class ExampleMessageListener : public MessageListenerConcurrently {
public:
    ConsumeStatus consumeMessage(const std::vector<MQMessageExt> &msgs) {
        for (auto item = msgs.begin(); item != msgs.end(); item++) {
            std::cout << "Consumer2 Received Message Topic:" << item->getTopic() << ", MsgId:" << item->getMsgId()
                      << ", TAGS:"
                      << item->getTags() << ", KEYS:" << item->getKeys() << ", Body:" << item->getBody() << std::endl;
        }
        // 消费成功返回CONSUME_SUCCESS
        return CONSUME_SUCCESS;
        // 消费失败返回RECONSUME_LATER，该消息将会被重新消费
        // return RECONSUME_LATER;
    }
};

int simplePushConsumer() {
    std::cout << "=======Before consuming messages=======" << std::endl;
    // 初始化默认消费者，并设置消费者组名称。
    DefaultMQPushConsumer *consumer = new DefaultMQPushConsumer("group11");
    // 设置服务地址
    consumer->setNamesrvAddr("endpoint");
    // 设置用户权限
    consumer->setSessionCredentials(
            "AK",
            "SK", "");
    // 设置命名空间
    consumer->setNameSpace("namespace");
    // 设置实例名称
    consumer->setInstanceName("cppClient");
    consumer->setConsumeMessageBatchMaxSize(1);
    // 设置为广播消费
    consumer->setMessageModel(MessageModel::BROADCASTING);
    auto start = std::chrono::system_clock::now();

    // 请注册自定义侦听函数用来处理接收到的消息，并返回响应的处理结果。
    ExampleMessageListener *messageListener = new ExampleMessageListener();
    // 订阅消息
    consumer->subscribe("topic1", "*");
    // 注册监听
    consumer->registerMessageListener(messageListener);

    // 准备工作完成，必须调用启动函数，才可以正常工作。
    consumer->start();

    // 请保持线程常驻，不要执行shutdown操作。
    std::this_thread::sleep_for(std::chrono::milliseconds(60 * 10000));
    consumer->shutdown();
    std::cout << "=======After consuming messages======" << std::endl;
    return 0;
}
