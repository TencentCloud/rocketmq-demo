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
		// 订阅 Topic 下的全部消息（tag 为 *）
		rmq_client.WithPushSubscriptionExpressions(map[string]*rmq_client.FilterExpression{
			Topic: rmq_client.SUB_ALL,
		}),
		rmq_client.WithPushMessageListener(&rmq_client.FuncMessageListener{
			Consume: func(mv *rmq_client.MessageView) rmq_client.ConsumerResult {
				fmt.Println("received message:", mv.GetMessageId(), mv.GetTopic(), string(mv.GetBody()))
				// 返回 SUCCESS 即确认消费成功（ack）
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

	fmt.Println("start receive message")

	exit := make(chan os.Signal, 1)
	signal.Notify(exit, syscall.SIGINT, syscall.SIGTERM)

	// wait for exit
	<-exit
}
