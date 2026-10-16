package main

import (
	"context"
	"fmt"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/apache/rocketmq-client-go/v2"
	"github.com/apache/rocketmq-client-go/v2/consumer"
	"github.com/apache/rocketmq-client-go/v2/primitive"
	"github.com/apache/rocketmq-client-go/v2/rlog"
)

// Pull 消费示例：由应用主动调用 Poll 拉取消息，处理后调用 ACK 确认。
// 注意：rocketmq-client-go 的 PullConsumer 目前仍是实验特性，接口后续版本可能变化，生产环境建议优先使用 PushConsumer。
func main() {
	// 设置SDK日志输出路径，注意，是绝对路径
	rlog.SetOutputPath("/logs/rocketmq-client-go.log")
	// topic名称
	var topicName = "topic1"
	// 消费组名称
	var groupName = "group11"

	nameserverReslover := primitive.NewPassthroughResolver([]string{"https://rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876"})

	// 创建 pull consumer
	c, err := rocketmq.NewPullConsumer(
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
		// 设置最大重试次数，ACK 时返回 ConsumeRetryLater 的消息会重试，超过后进入死信队列
		consumer.WithMaxReconsumeTimes(3),
	)
	if err != nil {
		fmt.Printf("init pull consumer error: %s\n", err.Error())
		os.Exit(1)
	}

	// 订阅topic，Expression 为 tag 过滤表达式，"*" 表示订阅所有消息
	err = c.Subscribe(topicName, consumer.MessageSelector{
		Type:       consumer.TAG,
		Expression: "*",
	})
	if err != nil {
		fmt.Printf("subscribe error: %s\n", err.Error())
		os.Exit(1)
	}
	// Note: start after subscribe
	err = c.Start()
	if err != nil {
		fmt.Printf("start pull consumer error: %s\n", err.Error())
		os.Exit(1)
	}

	// 收到 Ctrl+C / kill 信号后退出拉取循环
	stop := make(chan os.Signal, 1)
	signal.Notify(stop, syscall.SIGINT, syscall.SIGTERM)

loop:
	for {
		select {
		case <-stop:
			break loop
		default:
		}

		// 拉取消息，最多等待 5 秒
		cr, err := c.Poll(context.Background(), 5*time.Second)
		if consumer.IsNoNewMsgError(err) {
			// 暂时没有新消息
			continue
		}
		if err != nil {
			fmt.Printf("poll error: %s\n", err.Error())
			time.Sleep(time.Second)
			continue
		}

		for _, msg := range cr.GetMsgList() {
			fmt.Printf("received message: msgId=%s, queueId=%d, body=%s\n", msg.MsgId, msg.Queue.QueueId, string(msg.Body))
		}
		// 处理成功后确认；处理失败可返回 consumer.ConsumeRetryLater 让消息稍后重试
		c.ACK(context.Background(), cr, consumer.ConsumeSuccess)
	}

	// 关闭时会将内存中的消费位点提交到服务端
	err = c.Shutdown()
	if err != nil {
		fmt.Printf("shutdown pull consumer error: %s\n", err.Error())
	}
}
