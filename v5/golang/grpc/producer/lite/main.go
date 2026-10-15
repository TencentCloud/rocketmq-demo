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
	"context"
	"fmt"
	"log"
	"os"
	"strconv"
	"time"

	rmq_client "github.com/apache/rocketmq-clients/golang/v5"
	"github.com/apache/rocketmq-clients/golang/v5/credentials"
)

const (
	// Topic 控制台创建的「轻量消息」类型的一级 Topic 名称
	Topic = "xxxxxx"
	// Endpoint 填写腾讯云提供的接入地址
	Endpoint = "xxxxxx"
	// AccessKey 添加配置的ak
	AccessKey = "yourAccessKey"
	// SecretKey 添加配置的sk
	SecretKey = "yourSecretKey"
)

func main() {
	os.Setenv("mq.consoleAppender.enabled", "true")
	rmq_client.ResetLogger()
	// In most case, you don't need to create many producers, singleton pattern is more recommended.
	producer, err := rmq_client.NewProducer(&rmq_client.Config{
		Endpoint: Endpoint,
		Credentials: &credentials.SessionCredentials{
			AccessKey:    AccessKey,
			AccessSecret: SecretKey,
		},
	},
		rmq_client.WithTopics(Topic),
	)
	if err != nil {
		log.Fatal(err)
	}
	// start producer
	err = producer.Start()
	if err != nil {
		log.Fatal(err)
	}
	// graceful stop producer
	defer producer.GracefulStop()

	// 每条消息可以指定不同的 lite topic（二级 Topic），无需提前在控制台创建
	liteTopics := []string{"lite-topic-0", "lite-topic-1", "lite-topic-2"}
	for i := 0; i < 10; i++ {
		liteTopic := liteTopics[i%len(liteTopics)]
		// new a message
		msg := &rmq_client.Message{
			Topic: Topic,
			Body:  []byte("this is a lite message : " + strconv.Itoa(i)),
		}
		// set keys
		msg.SetKeys("key-" + strconv.Itoa(i))
		// set lite topic, it cannot be used with message group, delivery timestamp or priority
		msg.SetLiteTopic(liteTopic)
		// send message in sync
		resp, err := producer.Send(context.TODO(), msg)
		if err != nil {
			// 如果 lite topic 数量超过配额，需要评估并提升 lite topic 资源上限
			log.Fatal(err)
		}
		for i := 0; i < len(resp); i++ {
			fmt.Printf("liteTopic=%s, %#v\n", liteTopic, resp[i])
		}
		// wait a moment
		time.Sleep(time.Second * 1)
	}
}
