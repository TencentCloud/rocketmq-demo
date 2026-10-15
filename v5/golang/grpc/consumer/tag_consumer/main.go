/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	rmq_client "github.com/apache/rocketmq-clients/golang/v5"
	"github.com/apache/rocketmq-clients/golang/v5/credentials"
)

const (
	// Topic 控制台创建的「普通消息」类型的 Topic 名称
	Topic = "xxxxxx"
	// ConsumerGroup 控制台创建的消费组，并订阅上面的 Topic
	ConsumerGroup = "xxxxxx"
	// Endpoint 填写腾讯云提供的接入地址（控制台实例详情页获取）
	Endpoint = "rmq-xxx.rocketmq.xxxtencenttdmq.com:8080"
	// AccessKey 控制台「角色管理」中创建角色后获取的 ak
	AccessKey = "yourAccessKey"
	// SecretKey 控制台「角色管理」中创建角色后获取的 sk
	SecretKey = "yourSecretKey"
	// TagExpression tag 过滤表达式：多个 tag 用 || 分隔，* 表示订阅全部
	// 同一消费组内所有消费者的订阅关系（Topic + tag 表达式）必须保持一致
	TagExpression = "TagA||TagB"
)

var (
	// maximum waiting time for receive func
	awaitDuration = time.Second * 5
)

func main() {
	// log to console
	os.Setenv("mq.consoleAppender.enabled", "true")
	rmq_client.ResetLogger()
	// In most case, you don't need to create many consumers, singleton pattern is more recommended.
	pushConsumer, err := rmq_client.NewPushConsumer(&rmq_client.Config{
		Endpoint:      Endpoint,
		ConsumerGroup: ConsumerGroup,
		Credentials: &credentials.SessionCredentials{
			AccessKey:    AccessKey,
			AccessSecret: SecretKey,
		},
	},
		rmq_client.WithPushAwaitDuration(awaitDuration),
		// 按 tag 过滤（TAG 类型；NewFilterExpression 默认也是 TAG），只会收到 tag 为 TagA 或 TagB 的消息
		rmq_client.WithPushSubscriptionExpressions(map[string]*rmq_client.FilterExpression{
			Topic: rmq_client.NewFilterExpressionWithType(TagExpression, rmq_client.TAG),
		}),
		rmq_client.WithPushMessageListener(&rmq_client.FuncMessageListener{
			Consume: func(mv *rmq_client.MessageView) rmq_client.ConsumerResult {
				tag := ""
				if mv.GetTag() != nil {
					tag = *mv.GetTag()
				}
				fmt.Printf("received message: messageId=%s, tag=%s, body=%s\n", mv.GetMessageId(), tag, string(mv.GetBody()))
				// ack message
				return rmq_client.SUCCESS
			},
		}),
		rmq_client.WithPushConsumptionThreadCount(20),
		rmq_client.WithPushMaxCacheMessageCount(1024),
	)
	if err != nil {
		log.Fatal(err)
	}
	// start pushConsumer
	err = pushConsumer.Start()
	if err != nil {
		log.Fatal(err)
	}
	// graceful stop pushConsumer
	defer func() {
		if err := pushConsumer.GracefulStop(); err != nil {
			fmt.Println("graceful stop push consumer error: " + err.Error())
		}
	}()

	fmt.Println("start receive message, tag expression:", TagExpression)

	exit := make(chan os.Signal, 1)
	signal.Notify(exit, syscall.SIGINT, syscall.SIGTERM)

	// wait for exit
	<-exit
}
