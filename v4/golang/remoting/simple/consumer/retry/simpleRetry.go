package main

import (
	"context"
	"fmt"
	"os"
	"time"

	"github.com/apache/rocketmq-client-go/v2"
	"github.com/apache/rocketmq-client-go/v2/consumer"
	"github.com/apache/rocketmq-client-go/v2/primitive"
	"github.com/apache/rocketmq-client-go/v2/rlog"
)

func main() {
	// 设置SDK日志输出路径，注意，是绝对路径
	rlog.SetOutputPath("/logs/rocketmq-client-go.log")
	// topic名称
	var topicName = "topic1"
	// 消费组名称
	var groupName = "group11"

	nameserverReslover := primitive.NewPassthroughResolver([]string{"https://rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876"})

	traceCfg := &primitive.TraceConfig{
		Access:   primitive.Local,
		Resolver: nameserverReslover,
	} // 创建consumer
	c, err := rocketmq.NewPushConsumer(
		// 设置消费者组
		consumer.WithGroupName(groupName),
		// 设置服务地址
		consumer.WithNsResolver(nameserverReslover),
		// 设置acl权限
		consumer.WithCredentials(primitive.Credentials{
			SecretKey: "admin",
			AccessKey: "eyJrZXlJZC......",
		}),
		// 设置命名空间名称
		consumer.WithNamespace("rocketmq-xxx|namespace_go"),
		consumer.WithConsumerModel(consumer.Clustering),
		// 设置最大重试次数（不设置时默认16次），超过后消息进入死信队列 %DLQ%消费组名称
		consumer.WithMaxReconsumeTimes(5),

		// 设置trace, 用于发送消息时记录消息轨迹,如果不需要，不设置即可
		consumer.WithTrace(traceCfg),
	)
	if err != nil {
		fmt.Println("init consumer error: " + err.Error())
		os.Exit(0)
	}

	// 设置重新消费的延迟级别，共支持18中延迟级别。下面是延迟级别与延迟时间的对应关系
	// 1   2   3    4    5   6   7   8   9   10  11  12  13  14   15   16   17  18
	// 1s, 5s, 10s, 30s, 1m, 2m, 3m, 4m, 5m, 6m, 7m, 8m, 9m, 10m, 20m, 30m, 1h, 2h
	delayLevel := 1
	err = c.Subscribe(topicName, consumer.MessageSelector{}, func(ctx context.Context,
		msgs ...*primitive.MessageExt) (consumer.ConsumeResult, error) {
		fmt.Printf("subscribe callback len: %d \n", len(msgs))

		// 设置下次重试的延迟级别，仅在返回 consumer.ConsumeRetryLater 时生效
		if concurrentCtx, ok := primitive.GetConcurrentlyCtx(ctx); ok {
			concurrentCtx.DelayLevelWhenNextConsume = delayLevel
		}

		for _, msg := range msgs {
			// ReconsumeTimes 表示已重试次数，首次投递为0
			fmt.Printf("subscribe callback: msgId=%s, reconsumeTimes=%d, body=%s\n", msg.MsgId, msg.ReconsumeTimes, string(msg.Body))
		}
		// 模拟消费失败：返回 ConsumeRetryLater 触发重试，重试次数超过 WithMaxReconsumeTimes 后进入死信队列
		return consumer.ConsumeRetryLater, nil
	})
	if err != nil {
		fmt.Println(err.Error())
	}
	// Note: start after subscribe
	err = c.Start()
	if err != nil {
		fmt.Println(err.Error())
		os.Exit(-1)
	}
	time.Sleep(time.Hour)
	err = c.Shutdown()
	if err != nil {
		fmt.Printf("shundown Consumer error: %s", err.Error())
	}
}
