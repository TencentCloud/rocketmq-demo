#include <iostream>
#include <chrono>
#include <thread>
#include <include/MQMessageListener.h>
#include <include/DefaultMQPushConsumer.h>


using namespace std;
using namespace rocketmq;

int tagConsumer();

int main() {
    return tagConsumer();
}


// 消息监听
class TagMessageListener : public MessageListenerConcurrently {
public:
    ConsumeStatus consumeMessage(const std::vector<MQMessageExt> &msgs) {
        for (auto item = msgs.begin(); item != msgs.end(); item++) {
            // 业务，只会收到TagA或TagB的消息
            std::cout << "Received Message Topic:" << item->getTopic() << ", MsgId:" << item->getMsgId() << ", TAGS:"
                      << item->getTags() << ", KEYS:" << item->getKeys() << ", Body:" << item->getBody() << std::endl;
        }
        // 消费成功返回CONSUME_SUCCESS
        return CONSUME_SUCCESS;
    }
};

int tagConsumer() {
    std::cout << "=======Before consuming messages=======" << std::endl;
    // 初始化push消费者，并设置消费者组名称（需要在控制台创建消费组）
    DefaultMQPushConsumer *consumer = new DefaultMQPushConsumer("group");
    // 设置服务地址
    consumer->setNamesrvAddr("endpoint");
    // 设置用户权限
    consumer->setSessionCredentials(
            "AK",
            "SK",
            "");
    // 设置命名空间
    consumer->setNameSpace("namespace");
    long time = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    // 设置实例名称
    consumer->setInstanceName("CppClient" + to_string(time));
    consumer->setConsumeMessageBatchMaxSize(1);

    // 请注册自定义侦听函数用来处理接收到的消息，并返回响应的处理结果。
    TagMessageListener *messageListener = new TagMessageListener();
    // 订阅消息：设置订阅topic名称及tag表达式，多个tag用 "||" 分隔，"*" 表示订阅全部tag
    // 注意：同一消费组内所有实例的订阅关系（topic + tag）必须保持一致
    consumer->subscribe("topic", "TagA || TagB");
    // 设置消息监听
    consumer->registerMessageListener(messageListener);

    try {
        // 准备工作完成，必须调用启动函数，才可以正常工作。
        consumer->start();
    } catch (MQException &e) {
        std::cout << "Start consumer failed, ErrorCode: " << e.GetError() << " Exception:" << e.what() << std::endl;
        delete consumer;
        delete messageListener;
        return -1;
    }

    // 请保持线程常驻，不要执行shutdown操作。
    std::this_thread::sleep_for(std::chrono::milliseconds(60 * 10000));
    // 资源释放
    consumer->shutdown();
    delete consumer;
    delete messageListener;
    std::cout << "=======After consuming messages======" << std::endl;
    return 0;
}
