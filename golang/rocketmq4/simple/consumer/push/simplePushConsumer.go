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
	}
	// 创建consumer
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

		// 设置trace, 用于发送消息时记录消息轨迹,如果不需要，不设置即可
		consumer.WithTrace(traceCfg),
	)
	if err != nil {
		fmt.Println("init consumer error: " + err.Error())
		os.Exit(0)
	}

	// 订阅topic
	err = c.Subscribe(topicName, consumer.MessageSelector{}, func(ctx context.Context,
		msgs ...*primitive.MessageExt) (consumer.ConsumeResult, error) {
		fmt.Printf("subscribe callback: %v \n", msgs)
		return consumer.ConsumeSuccess, nil
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
		fmt.Printf("Shutdown Consumer error: %s", err.Error())
	}
}
