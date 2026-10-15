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
	// Topic 控制台创建的「普通消息」类型的 Topic 名称
	Topic = "xxxxxx"
	// Endpoint 填写腾讯云提供的接入地址（控制台实例详情页获取）
	Endpoint = "rmq-xxx.rocketmq.xxxtencenttdmq.com:8080"
	// AccessKey 控制台「角色管理」中创建角色后获取的 ak
	AccessKey = "yourAccessKey"
	// SecretKey 控制台「角色管理」中创建角色后获取的 sk
	SecretKey = "yourSecretKey"
)

// tags 轮流发送的 tag，配合 consumer/tag_consumer（订阅 TagA||TagB）使用，TagC 的消息不会被其消费
var tags = []string{"TagA", "TagB", "TagC"}

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

	for i := 0; i < 9; i++ {
		tag := tags[i%len(tags)]
		// new a message
		msg := &rmq_client.Message{
			Topic: Topic,
			Body:  []byte("this is a message with tag " + tag + " : " + strconv.Itoa(i)),
		}
		// 设置消息 tag，一条消息只能有一个 tag
		msg.SetTag(tag)
		msg.SetKeys("key-" + strconv.Itoa(i))
		// send message in sync
		resp, err := producer.Send(context.TODO(), msg)
		if err != nil {
			log.Fatal(err)
		}
		for j := 0; j < len(resp); j++ {
			fmt.Printf("send message success, tag=%s, messageId=%s\n", tag, resp[j].MessageID)
		}
		// wait a moment
		time.Sleep(time.Second * 1)
	}
}
