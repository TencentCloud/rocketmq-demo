#include <iostream>
#include <chrono>
#include <thread>
#include "include/TransactionMQProducer.h"
#include "include/MQClientException.h"
#include "include/TransactionListener.h"

using namespace std;
using namespace rocketmq;

class ExampleTransactionListener : public TransactionListener {
public:
    LocalTransactionState executeLocalTransaction(const MQMessage &msg, void *arg) {
        // 执行本地事务，成功返回COMMIT_MESSAGE，失败返回ROLLBACK_MESSAGE，不确定返回UNKNOWN
        // 如果返回UNKNOWN，则会触发定时任务回查状态
        std::cout << "Execute Local Transaction,Received Message Topic:" << msg.getTopic() << ", MsgId:"
                  << msg.getBody() << std::endl;
        return UNKNOWN;
    }

    LocalTransactionState checkLocalTransaction(const MQMessageExt &msg) {
        // 回查本地事务执行情况，成功返回COMMIT_MESSAGE，失败返回ROLLBACK_MESSAGE，不确定返回UNKNOWN。
        // 如果返回UNKNOWN，则等待下次定时任务回查。
        std::cout << "Check Local Transaction,Received Message Topic:" << msg.getTopic() << ", MsgId:" << msg.getMsgId()
                  << std::endl;
        return COMMIT_MESSAGE;
    }
};

int main() {
    std::cout << "=======Before sending messages=======" << std::endl;
    // 初始化事务消息生产者，并设置生产者组名称
    TransactionMQProducer producer("transaction_group");
    // 设置服务地址
    producer.setNamesrvAddr("endpoint");
    // 设置用户权限
    producer.setSessionCredentials(
            "AK",
            "SK",
            "");
    // 设置命名空间
    producer.setNameSpace("namespace");
    // 请确保参数设置完成在启动之前e
    producer.start();

    //本地事务执行和回查函数。
    ExampleTransactionListener *exampleTransactionListener = new ExampleTransactionListener();
    producer.setTransactionListener(exampleTransactionListener);
    //请确保参数设置完成之后启动Producer。
    producer.start();
    auto start = std::chrono::system_clock::now();
    int count = 3;
    for (int i = 0; i < count; ++i) {
        //发送消息时请设置您在阿里云消息队列RocketMQ版控制台上申请的Topic。
        MQMessage msg("transaction_topic", "your tag", "Hello,CPP SDK, Transaction Message.");
        try {
            SendResult sendResult = producer.sendMessageInTransaction(msg, &i);
            std::cout << "SendResult:" << sendResult.getSendStatus() << ", Message ID: " << sendResult.getMsgId()
                      << std::endl;
            this_thread::sleep_for(chrono::seconds(1));
        } catch (MQException e) {
            std::cout << "ErrorCode: " << e.GetError() << " Exception:" << e.what() << std::endl;
        }
    }
    auto interval = std::chrono::system_clock::now() - start;
    std::cout << "Send " << count << " messages OK, costs "
              << std::chrono::duration_cast<std::chrono::milliseconds>(interval).count() << "ms" << std::endl;

    std::cout << "Wait for local transaction check..... " << std::endl;
    for (int i = 0; i < 6; ++i) {
        this_thread::sleep_for(chrono::seconds(10));
        std::cout << "Running "<< i*10 + 10 << " Seconds......"<< std::endl;
    }
    producer.shutdown();
    std::cout << "=======After sending messages=======" << std::endl;
    return 0;
}