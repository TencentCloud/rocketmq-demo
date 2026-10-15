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
	// Topic 控制台创建的「轻量消息」类型的一级 Topic 名称
	Topic = "xxxxxx"
	// ConsumerGroup 控制台创建的消费组，需选择「Lite Topic 消费」模式并绑定到上面的一级 Topic
	ConsumerGroup = "xxxxxx"
	// Endpoint 填写腾讯云提供的接入地址
	Endpoint = "xxxxxx"
	// AccessKey 添加配置的ak
	AccessKey = "yourAccessKey"
	// SecretKey 添加配置的sk
	SecretKey = "yourSecretKey"
)

var (
	// maximum waiting time for receive func
	awaitDuration = time.Second * 5
	// invisible duration of received messages
	invisibleDuration = time.Second * 30
)

func main() {
	// log to console
	os.Setenv("mq.consoleAppender.enabled", "true")
	rmq_client.ResetLogger()
	// In most case, you don't need to create many consumers, singleton pattern is more recommended.
	litePushConsumer, err := rmq_client.NewLitePushConsumer(&rmq_client.Config{
		Endpoint:      Endpoint,
		ConsumerGroup: ConsumerGroup,
		Credentials: &credentials.SessionCredentials{
			AccessKey:    AccessKey,
			AccessSecret: SecretKey,
		},
	},
		// 绑定一级 Topic，后续订阅的 lite topic 都属于该 Topic
		rmq_client.NewLitePushConsumerConfig(Topic, invisibleDuration),
		rmq_client.WithPushAwaitDuration(awaitDuration),
		rmq_client.WithPushMessageListener(&rmq_client.FuncMessageListener{
			Consume: func(mv *rmq_client.MessageView) rmq_client.ConsumerResult {
				fmt.Println("received message:", mv.GetMessageId(), mv.GetTopic(), mv.GetLiteTopic(), string(mv.GetBody()))
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
	// start litePushConsumer
	err = litePushConsumer.Start()
	if err != nil {
		log.Fatal(err)
	}
	// graceful stop litePushConsumer
	defer litePushConsumer.GracefulStop()

	// SubscribeLite 会发起网络请求并校验配额，可能失败，需要检查返回值：
	// 1. 网络请求失败，可以稍后重试；
	// 2. 配额校验失败，需要评估配额是否足够，并及时通过 UnSubscribeLite 取消不再使用的订阅以释放配额。
	for _, liteTopic := range []string{"lite-topic-0", "lite-topic-1", "lite-topic-2"} {
		if err := litePushConsumer.SubscribeLite(liteTopic); err != nil {
			log.Printf("subscribe lite topic %s error: %v", liteTopic, err)
			continue
		}
		fmt.Println("subscribed lite topic:", liteTopic)
	}
	// 不再需要时取消订阅，释放配额
	// _ = litePushConsumer.UnSubscribeLite("lite-topic-2")

	exit := make(chan os.Signal, 1)
	signal.Notify(exit, syscall.SIGINT, syscall.SIGTERM)

	// wait for exit
	<-exit
}
