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

// 重试消息示例：PushConsumer 的监听器返回 FAILURE 即视为消费失败，服务端会按消费组的重试策略重新投递该消息。
// 最大重试次数由消费组的重试策略决定（控制台「消费组」配置中设置），超过最大重试次数后消息进入死信队列。
// 注意：生产者的 WithMaxAttempts 是「发送失败」时的重试次数，与这里的消费重试无关。
const (
	// Topic 控制台创建的「普通消息」类型的 Topic 名称
	Topic = "xxxxxx"
	// ConsumerGroup 控制台创建的消费组，并订阅上面的 Topic；最大重试次数在该消费组的重试策略中配置
	ConsumerGroup = "xxxxxx"
	// Endpoint 填写腾讯云提供的接入地址（控制台实例详情页获取）
	Endpoint = "rmq-xxx.rocketmq.xxxtencenttdmq.com:8080"
	// AccessKey 控制台「角色管理」中创建角色后获取的 ak
	AccessKey = "yourAccessKey"
	// SecretKey 控制台「角色管理」中创建角色后获取的 sk
	SecretKey = "yourSecretKey"
	// successAttempt 第几次投递时返回 SUCCESS，用于演示前几次失败触发重试
	successAttempt = 3
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
		rmq_client.WithPushSubscriptionExpressions(map[string]*rmq_client.FilterExpression{
			Topic: rmq_client.SUB_ALL,
		}),
		rmq_client.WithPushMessageListener(&rmq_client.FuncMessageListener{
			Consume: func(mv *rmq_client.MessageView) rmq_client.ConsumerResult {
				// GetDeliveryAttempt 返回当前是第几次投递，首次投递为 1，每重试一次加 1
				attempt := mv.GetDeliveryAttempt()
				fmt.Printf("received message: messageId=%s, deliveryAttempt=%d, body=%s\n",
					mv.GetMessageId(), attempt, string(mv.GetBody()))
				if attempt < successAttempt {
					// 返回 FAILURE 触发重试，消息会在退避时间后重新投递
					fmt.Printf("consume failed, messageId=%s will be redelivered\n", mv.GetMessageId())
					return rmq_client.FAILURE
				}
				// 返回 SUCCESS 确认消费成功，不再重试
				fmt.Printf("consume success, messageId=%s\n", mv.GetMessageId())
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
