cmake_minimum_required(VERSION 3.24)
project(rocketmq5)

set(CMAKE_CXX_STANDARD 14)

include_directories(/path/to/sdk/include)
link_directories(/path/to/sdk/build)

find_package(ZLIB REQUIRED)

function(add_example name file)
    add_executable(${name} ${file})
    target_link_libraries(${name} PRIVATE ZLIB::ZLIB rocketmq)
endfunction()

add_example(example_producer ExampleProducer.cpp)
add_example(example_producer_with_async ExampleProducerWithAsync.cpp)
add_example(example_producer_with_fifo_message ExampleProducerWithFifoMessage.cpp)
add_example(example_producer_with_timed_message ExampleProducerWithTimedMessage.cpp)
add_example(example_producer_with_transactional_message ExampleProducerWithTransactionalMessage.cpp)
add_example(example_push_consumer ExamplePushConsumer.cpp)
add_example(example_simple_consumer ExampleSimpleConsumer.cpp)