package main

import (
	"context"
	"fmt"
	"github.com/apache/rocketmq-client-go/v2"
	"github.com/apache/rocketmq-client-go/v2/primitive"
	"github.com/apache/rocketmq-client-go/v2/producer"
	"os"
	"strconv"
	"time"
)

func main() {
	// topic名称
	var topicName = "topic1"
	// 生产者组名称
	var groupName = "group1"
	// 创建消息生产者
	p, _ := rocketmq.NewProducer(
		// 设置服务地址
		producer.WithNsResolver(primitive.NewPassthroughResolver([]string{"https://rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876"})),
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
	)
	// 启动producer
	err := p.Start()
	if err != nil {
		fmt.Printf("start producer error: %s", err.Error())
		os.Exit(1)
	}

	for i := 0; i < 1; i++ {
		msg := primitive.NewMessage(topicName, []byte("Hello RocketMQ Go Client! This is a delay message."))
		// 设置延迟等级
		// 等级与时间对应关系：
		// 1s、 5s、 10s、 30s、  1m、 2m、 3m、 4m、 5m、 6m、 7m、 8m、 9m、 10m、 20m、 30m、 1h、 2h；
		// 1    2    3     4     5    6   7    8   9   10   11   12  13   14    15    16   17   18
		//如果想用延迟级别，那么设置下面这个方法
		msg.WithDelayTimeLevel(3)

		//如果想用任意延迟消息，那么设置下面这个方法，WithDelayTimeLevel 就不要设置了,单位为具体的毫秒，如下则是10s后投递
		delayMills := int64(10 * 1000)
		msg.WithProperty("__STARTDELIVERTIME", strconv.FormatInt(time.Now().Unix()+delayMills, 10))
		// 发送消息
		res, err := p.SendSync(context.Background(), msg)

		if err != nil {
			fmt.Printf("send message error: %s\n", err)
		} else {
			fmt.Printf("send message success: result=%s\n", res.String())
		}
	}
	// 释放资源
	err = p.Shutdown()
	if err != nil {
		fmt.Printf("shutdown producer error: %s", err.Error())
	}
}
