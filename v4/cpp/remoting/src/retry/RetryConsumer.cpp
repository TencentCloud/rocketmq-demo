#include <iostream>
#include <chrono>
#include <thread>
#include <include/MQMessageListener.h>
#include <include/DefaultMQPushConsumer.h>


using namespace std;
using namespace rocketmq;

/**
 * 重试消息（消费重试）示例
 * 集群消费模式下，消费返回RECONSUME_LATER（或监听器抛出异常）时，消息会被重新投递，重试间隔逐次递增；
 * 超过最大重试次数（默认16次，可通过setMaxReconsumeTimes设置）后，消息进入死信队列 %DLQ%消费组名称。
 * 广播消费模式下消费失败不会重试。
 * 注意：生产者的setRetryTimes是发送失败重试，与此处的消费重试无关。
 */
int retryConsumer();

int main() {
    return retryConsumer();
}


// 消息监听
class RetryMessageListener : public MessageListenerConcurrently {
public:
    ConsumeStatus consumeMessage(const std::vector<MQMessageExt> &msgs) {
        for (auto item = msgs.begin(); item != msgs.end(); item++) {
            // getReconsumeTimes() 为该消息已重试的次数，首次投递为0
            std::cout << "Received Message Topic:" << item->getTopic() << ", MsgId:" << item->getMsgId()
                      << ", ReconsumeTimes:" << item->getReconsumeTimes() << ", TAGS:" << item->getTags()
                      << ", KEYS:" << item->getKeys() << ", Body:" << item->getBody() << std::endl;
        }
        // 模拟消费失败：返回RECONSUME_LATER，该消息将会被重新消费
        return RECONSUME_LATER;
    }
};

int retryConsumer() {
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
    // 集群消费模式（默认值），只有集群模式下消费失败才会重试
    consumer->setMessageModel(MessageModel::CLUSTERING);
    // 设置最大重试次数（不设置时默认16次），超过后消息进入死信队列 %DLQ%消费组名称
    consumer->setMaxReconsumeTimes(3);

    // 请注册自定义侦听函数用来处理接收到的消息，并返回响应的处理结果。
    RetryMessageListener *messageListener = new RetryMessageListener();
    // 订阅消息 （设置订阅topic名称及tag信息）
    consumer->subscribe("topic", "*");
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
