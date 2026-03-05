#include <iostream>
#include <chrono>
#include <thread>
#include "include/DefaultMQProducer.h"

using namespace std;
using namespace rocketmq;

/**
 * 顺序消息使用顺序类型的topic配置简单的生产者与消费者也可实现顺序消息
 * 顺序类型的topic支持全局顺序和局部顺序两类类型
 */


class ExampleSelectMessageQueueByHash : public MessageQueueSelector {
public:
    MQMessageQueue select(const std::vector<MQMessageQueue> &mqs, const MQMessage &msg, void *arg) {
        // 实现自定义分区逻辑，根据业务传入arg参数即分区键，计算路由到哪个队列，这里以arg为int型参数为例。
        int orderId = *static_cast<int *>(arg);
        int index = orderId % mqs.size();
        return mqs[0];
    }
};

int main() {
    std::cout << "=======Before sending messages=======" << std::endl;
    // 初始化消息生产者，并设置生产者组名称
    DefaultMQProducer producer("group1");
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
    // 请确保参数设置完成之后启动Producer。
    producer.start();
    auto start = std::chrono::system_clock::now();
    int count = 10;
    // 可以设置发送重试的次数，确保发送成功。
    int retryTimes = 1;
    // 参考接口MessageQueueSelector，通过设置的自定义参数arg，计算发送到指定的路由队列中，此处的arg便是分区ID。
    ExampleSelectMessageQueueByHash *pSelector = new ExampleSelectMessageQueueByHash();
    for (int i = 0; i < count; ++i) {
        // 发送消息时请设置您在阿里云消息队列RocketMQ控制台上申请的Topic。
        MQMessage msg("order_topic", "your tags", "your keys", "Hello,CPP SDK, Orderly Message.");
        try {
            SendResult sendResult = producer.send(msg, pSelector, &i, retryTimes, false);
            std::cout << "SendResult:" << sendResult.getSendStatus() << ", Message ID: " << sendResult.getMsgId()
                      << "MessageQueue:" << sendResult.getMessageQueue().toString() << std::endl;
            this_thread::sleep_for(chrono::seconds(1));
        } catch (MQException e) {
            std::cout << "ErrorCode: " << e.GetError() << " Exception:" << e.what() << std::endl;
        }
    }
    auto interval = std::chrono::system_clock::now() - start;
    std::cout << "Send " << count << " messages OK, costs "
              << std::chrono::duration_cast<std::chrono::milliseconds>(interval).count() << "ms" << std::endl;
    // 资源释放
    producer.shutdown();
    std::cout << "=======After sending messages=======" << std::endl;
    return 0;
}