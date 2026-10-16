package main

import (
	"context"
	"fmt"
	"os"
	"strconv"
	"sync"
	"time"

	"github.com/apache/rocketmq-client-go/v2"
	"github.com/apache/rocketmq-client-go/v2/primitive"
	"github.com/apache/rocketmq-client-go/v2/producer"
	"github.com/apache/rocketmq-client-go/v2/rlog"
)

// 事务消息生产者：需要在控制台创建事务类型的topic
// 发送半消息成功后执行本地事务（ExecuteLocalTransaction），根据结果提交或回滚；
// 本地事务状态未知时，服务端会回查（CheckLocalTransaction），生产者需要保持运行才能响应回查
// 事务消息提交后，使用普通的消费者（如 simple/consumer/push）订阅该topic即可消费

// OrderTransactionListener 实现 primitive.TransactionListener 接口
type OrderTransactionListener struct {
	// 模拟本地事务结果存储，key为业务key，实际业务中应查询数据库等持久化存储
	localTrans *sync.Map
}

// ExecuteLocalTransaction 半消息发送成功后执行本地事务
func (l *OrderTransactionListener) ExecuteLocalTransaction(msg *primitive.Message) primitive.LocalTransactionState {
	bizKey := msg.GetKeys()
	index, err := strconv.Atoi(msg.GetProperty("index"))
	if err != nil {
		fmt.Printf("parse index error, rollback. key=%s, err=%s\n", bizKey, err)
		return primitive.RollbackMessageState
	}
	// 模拟本地事务结果：0 提交，1 回滚，2 状态未知（等待服务端回查）
	var state primitive.LocalTransactionState
	switch index % 3 {
	case 0:
		state = primitive.CommitMessageState
	case 1:
		state = primitive.RollbackMessageState
	default:
		state = primitive.UnknowState
	}
	// 记录本地事务最终结果，供回查使用（这里假设状态未知的事务最终提交成功）
	if state == primitive.UnknowState {
		l.localTrans.Store(bizKey, primitive.CommitMessageState)
	} else {
		l.localTrans.Store(bizKey, state)
	}
	fmt.Printf("execute local transaction: key=%s, transactionId=%s, state=%d\n", bizKey, msg.TransactionId, state)
	return state
}

// CheckLocalTransaction 服务端回查本地事务状态
func (l *OrderTransactionListener) CheckLocalTransaction(msg *primitive.MessageExt) primitive.LocalTransactionState {
	bizKey := msg.GetKeys()
	v, existed := l.localTrans.Load(bizKey)
	if !existed {
		// 查不到本地事务记录（如生产者重启导致内存数据丢失），返回未知等待下次回查
		fmt.Printf("check local transaction: key=%s, msgId=%s not found, return unknown\n", bizKey, msg.MsgId)
		return primitive.UnknowState
	}
	state := v.(primitive.LocalTransactionState)
	fmt.Printf("check local transaction: key=%s, msgId=%s, state=%d\n", bizKey, msg.MsgId, state)
	return state
}

func main() {
	// 设置SDK日志输出路径，注意，是绝对路径
	rlog.SetOutputPath("/logs/rocketmq-client-go.log")
	// topic名称，需要在控制台创建事务类型的topic
	var topicName = "topic1"
	// 生产者组名称
	var groupName = "group1"

	nameserverReslover := primitive.NewPassthroughResolver([]string{"https://rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876"})

	traceCfg := &primitive.TraceConfig{
		Access:   primitive.Local,
		Resolver: nameserverReslover,
	}
	// 创建事务消息生产者，第一个参数为事务监听器
	p, err := rocketmq.NewTransactionProducer(
		&OrderTransactionListener{localTrans: new(sync.Map)},
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
	if err != nil {
		fmt.Printf("init transaction producer error: %s\n", err.Error())
		os.Exit(1)
	}
	// 启动producer
	err = p.Start()
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
			Body: []byte("Hello RocketMQ Go Client! This is a transaction message" + strconv.Itoa(i)),
		}
		// 设置业务key，本示例用它关联本地事务记录
		msg.WithKeys([]string{"transKey" + strconv.Itoa(i)})
		// 自定义属性，用于在本地事务中模拟不同的事务结果
		msg.WithProperty("index", strconv.Itoa(i))
		// 发送事务消息
		res, err := p.SendMessageInTransaction(context.Background(), msg)

		if err != nil {
			fmt.Printf("send transaction message error: %s\n", err)
		} else {
			fmt.Printf("send transaction message success: state=%d, result=%s\n", res.State, res.String())
		}
	}
	// 保持运行一段时间，等待服务端对状态未知的事务发起回查
	time.Sleep(5 * time.Minute)
	// 释放资源
	err = p.Shutdown()
	if err != nil {
		fmt.Printf("shutdown producer error: %s", err.Error())
	}
}
