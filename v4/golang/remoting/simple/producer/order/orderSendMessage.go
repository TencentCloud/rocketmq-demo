package main

import (
	"context"
	"fmt"
	"os"

	"github.com/apache/rocketmq-client-go/v2"
	"github.com/apache/rocketmq-client-go/v2/primitive"
	"github.com/apache/rocketmq-client-go/v2/producer"
	"github.com/apache/rocketmq-client-go/v2/rlog"
)

// 顺序消息生产者：需要在控制台创建顺序类型的topic
// 同一个业务key（如订单号）通过 ShardingKey 哈希到同一个队列，保证同一key内的消息按发送顺序写入队列
// 消费端需要使用顺序消费：创建PushConsumer时设置 consumer.WithConsumerOrder(true)，才能保证同一队列内的消息按顺序消费

func main() {
	// 设置SDK日志输出路径，注意，是绝对路径
	rlog.SetOutputPath("/logs/rocketmq-client-go.log")
	// topic名称，需要在控制台创建顺序类型的topic
	var topicName = "topic1"
	// 生产者组名称
	var groupName = "group1"

	nameserverReslover := primitive.NewPassthroughResolver([]string{"https://rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876"})

	traceCfg := &primitive.TraceConfig{
		Access:   primitive.Local,
		Resolver: nameserverReslover,
	}
	// 创建消息生产者
	p, err := rocketmq.NewProducer(
		// 设置服务地址
		producer.WithNsResolver(nameserverReslover),
		// 设置acl权限
		producer.WithCredentials(primitive.Credentials{
			SecretKey: "admin",
			AccessKey: "eyJrZXlJZC......",
		}),
		// 设置生产组
		producer.WithGroupName(groupName),
		// 设置命名空间名称
		producer.WithNamespace("rocketmq-xxx|namespace_go"),
		// 设置发送失败重试次数
		producer.WithRetry(2),
		// 设置队列选择器：按消息的 ShardingKey 哈希选择队列，同一key始终发送到同一个队列
		producer.WithQueueSelector(producer.NewHashQueueSelector()),

		// 设置trace, 用于发送消息时记录消息轨迹,如果不需要，不设置即可
		producer.WithTrace(traceCfg),
	)
	if err != nil {
		fmt.Printf("init producer error: %s\n", err.Error())
		os.Exit(1)
	}
	// 启动producer
	err = p.Start()
	if err != nil {
		fmt.Printf("start producer error: %s", err.Error())
		os.Exit(1)
	}

	// 模拟3个订单，每个订单依次产生 创建、支付、发货、完成 4个步骤的消息
	orderIds := []string{"order-1001", "order-1002", "order-1003"}
	steps := []string{"create", "pay", "ship", "finish"}
	for _, step := range steps {
		for _, orderId := range orderIds {
			// 构造消息内容
			msg := &primitive.Message{
				// 设置topic名称
				Topic: topicName,
				// 消息内容
				Body: []byte("Hello RocketMQ Go Client! This is an order message, orderId=" + orderId + ", step=" + step),
			}
			// 设置分区key（ShardingKey），相同key的消息会被发送到同一个队列
			msg.WithShardingKey(orderId)
			// 设置key，便于在控制台按key查询消息
			msg.WithKeys([]string{orderId})
			// 同步发送消息，顺序消息需要同步发送，上一条发送成功后再发下一条
			res, err := p.SendSync(context.Background(), msg)

			if err != nil {
				fmt.Printf("send message error: orderId=%s, step=%s, err=%s\n", orderId, step, err)
			} else {
				fmt.Printf("send message success: orderId=%s, step=%s, queueId=%d, result=%s\n",
					orderId, step, res.MessageQueue.QueueId, res.String())
			}
		}
	}
	// 释放资源
	err = p.Shutdown()
	if err != nil {
		fmt.Printf("shutdown producer error: %s", err.Error())
	}
}
