#include <iostream>
#include <chrono>
#include <thread>
#include <include/DefaultMQProducer.h>
#include <include/DefaultMQPullConsumer.h>
#include <atomic>


using namespace std;
using namespace rocketmq;

int simplePullConsumer();

int main() {
    simplePullConsumer();
}

// 记录消费偏移量
std::map<MQMessageQueue, uint64_t> g_offseTable;

// 设置队列偏移量
void putMessageQueueOffset(MQMessageQueue mq, uint64_t offset) {
    g_offseTable[mq] = offset;
}

// 获取偏移量
uint64_t getMessageQueueOffset(MQMessageQueue mq) {
    map<MQMessageQueue, uint64_t>::iterator it = g_offseTable.find(mq);
    if (it != g_offseTable.end()) {
        return it->second;
    }
    return 0;
}

// 输出pull结果信息
void PrintPullResult(rocketmq::PullResult *result) {
    std::cout << result->toString() << std::endl;
    if (result->pullStatus == rocketmq::FOUND) {
        std::cout << result->toString() << endl;
        std::vector<rocketmq::MQMessageExt>::iterator it = result->msgFoundList.begin();
        for (; it != result->msgFoundList.end(); ++it) {
            cout << "=======================================================" << endl << (*it).toString() << endl;
        }
    }
}

int simplePullConsumer() {
    std::cout << "=======Before consuming messages=======" << std::endl;
    // 初始话pull消费者
    DefaultMQPullConsumer *consumer = new DefaultMQPullConsumer("group11");
    // 设置服务地址
    consumer->setNamesrvAddr("endpoint");
    // 设置用户权限
    consumer->setSessionCredentials(
            "AK",
            "SK",
            "");
    // 设置命名空间
    consumer->setNameSpace("namespace");
    // 设置客户端实力名称
    consumer->setInstanceName("cppPullClient");
    // 注册空监听
    consumer->registerMessageQueueListener("topic1", NULL);
    //准备工作完成，必须调用启动函数，才可以正常工作。
    consumer->start();
    auto start = std::chrono::system_clock::now();
    std::atomic<int> g_msgCount(1);
    std::vector<MQMessageQueue> mqs;

    try {
        // 获取topic的队列信息
        consumer->fetchSubscribeMessageQueues("topic1", mqs);
        auto iter = mqs.begin();
        for (; iter != mqs.end(); ++iter) {
            std::cout << "mq:" << (*iter).toString() << endl;
        }
    } catch (MQException &e) {
        std::cout << e << endl;
    }

    auto iter = mqs.begin();
    for (; iter != mqs.end(); ++iter) {
        MQMessageQueue mq = (*iter);
        // if cluster model
        putMessageQueueOffset(mq, consumer->fetchConsumeOffset(mq, true));
        // if broadcast model
        // putMessageQueueOffset(mq, your last consume offset);
        bool noNewMsg = false;
        do {
            try {
                // 从队列中pull消息
                PullResult result = consumer->pull(mq, "*", getMessageQueueOffset(mq), 64);
                g_msgCount += result.msgFoundList.size();
                std::cout << result.msgFoundList.size() << std::endl;
                // if pull request timeout or received NULL response, pullStatus will be
                // setted to BROKER_TIMEOUT,
                // And nextBeginOffset/minOffset/MaxOffset will be setted to 0
                if (result.pullStatus != BROKER_TIMEOUT) {
                    putMessageQueueOffset(mq, result.nextBeginOffset);
                    PrintPullResult(&result);
                } else {
                    cout << "broker timeout occur" << endl;
                }
                switch (result.pullStatus) {
                    case FOUND:
                    case NO_MATCHED_MSG:
                    case OFFSET_ILLEGAL:
                    case BROKER_TIMEOUT:
                        break;
                    case NO_NEW_MSG:
                        noNewMsg = true;
                        break;
                    default:
                        break;
                }
            } catch (MQClientException &e) {
                std::cout << e << std::endl;
            }
        } while (!noNewMsg);
    }
    // 资源释放
    consumer->shutdown();
    auto end = std::chrono::system_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
    std::cout << "=======After consuming messages======" << std::endl;
    std::cout << "msg count: " << g_msgCount.load() << "\n";
    std::cout << "per msg time: " << duration.count() / (double) g_msgCount.load() << "ms \n"
              << "========================finished==============================\n";
    return 0;
}