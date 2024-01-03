package main

import (
	"context"
	"fmt"
	"os"
	"strconv"

	"github.com/apache/rocketmq-client-go/v2"
	"github.com/apache/rocketmq-client-go/v2/primitive"
	"github.com/apache/rocketmq-client-go/v2/producer"
	"github.com/apache/rocketmq-client-go/v2/rlog"
)

func main() {
	// 设置SDK日志输出路径，注意，是绝对路径
	rlog.SetOutputPath("/logs/rocketmq-client-go.log")
	// topic名称
	var topicName = "topic1"
	// 生产者组名称
	var groupName = "group1"

	nameserverReslover := primitive.NewPassthroughResolver([]string{"https://rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876"})

	traceCfg := &primitive.TraceConfig{
		Access:   primitive.Local,
		Resolver: nameserverReslover,
	}
	// 创建消息生产者
	p, _ := rocketmq.NewProducer(
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

		// 设置trace, 用于发送消息时记录消息轨迹,如果不需要，不设置即可
		producer.WithTrace(traceCfg),
	)
	// 启动producer
	err := p.Start()
	if err != nil {
		fmt.Printf("start producer error: %s", err.Error())
		os.Exit(1)
	}

	for i := 0; i < 10; i++ {
		// 构造消息内容
		msg := &primitive.Message{
			// 设置topic名称
			Topic: topicName,
			// 消息内容
			Body: []byte("Hello RocketMQ Go Client! This is a sync message" + strconv.Itoa(i)), // 消息内容
		}
		// 发送消息
		res, err := p.SendSync(context.Background(), msg)

		if err != nil {
			fmt.Printf("send message error: %s\n", err)
		} else {
			fmt.Printf("send message success: result=%s\n", res.String())
		}
	}
	err = p.Shutdown()
	if err != nil {
		fmt.Printf("shutdown producer error: %s", err.Error())
	}
}
