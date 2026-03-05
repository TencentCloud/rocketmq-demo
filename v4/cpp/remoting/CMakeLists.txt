cmake_minimum_required(VERSION 3.20)
project(rocketmq_demo)

set(CMAKE_CXX_STANDARD 14)

# 设置头文件地址
set(INC_DIR /opt/newrockermq/rocketmq-client-cpp/)

# 设置动态库地址
set(LINK_DIR /opt/newrockermq/rocketmq-client-cpp/bin)

include_directories(${INC_DIR})

link_directories(${LINK_DIR})

# 生产者
add_executable(syncSendMessage src/simple/SyncSendMessage.cpp)
add_executable(asyncSendMessage src/simple/AsyncSendMessage.cpp)
add_executable(onewaySendMessage src/simple/OnewaySendMessage.cpp)
add_executable(batchSendMessage src/simple/BatchSendMessage.cpp)

# 消费者
add_executable(pushConsumer src/simple/PushConsumer.cpp)
add_executable(pullConsumer src/simple/PullConsumer.cpp)

add_executable(broadcastConsumer1 src/broadcast/Consumer1.cpp)
add_executable(broadcastConsumer2 src/broadcast/Consumer2.cpp)

add_executable(delayConsumer src/delay/Consumer.cpp)
add_executable(delayProducer src/delay/SendDelayMessage.cpp)
add_executable(scheduledMessage src/delay/SendScheduledMessage.cpp)

add_executable(orderConsumer src/order/Consumer.cpp)
add_executable(orderProducer src/order/Producer.cpp)

add_executable(transactionProducer src/transaction/TransactionProducer.cpp)
add_executable(transactionConsumer src/transaction/TransactionConsumer.cpp)


# 将库链接到项目中
target_link_libraries(syncSendMessage rocketmq.so)
target_link_libraries(asyncSendMessage rocketmq.so)
target_link_libraries(onewaySendMessage rocketmq.so)
target_link_libraries(batchSendMessage rocketmq.so)

target_link_libraries(pushConsumer rocketmq.so)
target_link_libraries(pullConsumer rocketmq.so)

target_link_libraries(broadcastConsumer1 rocketmq.so)
target_link_libraries(broadcastConsumer2 rocketmq.so)

target_link_libraries(delayConsumer rocketmq.so)
target_link_libraries(delayProducer rocketmq.so)
target_link_libraries(scheduledMessage rocketmq.so)

target_link_libraries(orderConsumer rocketmq.so)
target_link_libraries(orderProducer rocketmq.so)

target_link_libraries(transactionProducer rocketmq.so)
target_link_libraries(transactionConsumer rocketmq.so)