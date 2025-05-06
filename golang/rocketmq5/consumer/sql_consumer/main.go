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
	"os/signal"
	"sync"
	"syscall"
	"time"

	rmq_client "github.com/apache/rocketmq-clients/golang/v5"
	"github.com/apache/rocketmq-clients/golang/v5/credentials"
)

const (
	Topic         = "xxxxxx"
	ConsumerGroup = "xxxxxx"
	// Endpoint 填写腾讯云提供的接入地址
	Endpoint = "xxxxxx"
	// AccessKey 添加配置的ak
	AccessKey = "AKID**********************0123456789"
	// SecretKey 添加配置的sk
	SecretKey = "sk0123456789********************"
)

var (
	// maximum waiting time for receive func
	awaitDuration = time.Second * 5
	// maximum number of messages received at one time
	maxMessageNum int32 = 16
	// invisibleDuration should > 20s
	invisibleDuration = time.Second * 20
	// receive concurrency
	receiveConcurrency = 6
)

func main() {
	// log to console
	os.Setenv("mq.consoleAppender.enabled", "true")
	rmq_client.ResetLogger()
	// In most case, you don't need to create many consumers, singleton pattern is more recommended.
	simpleConsumer, err := rmq_client.NewSimpleConsumer(&rmq_client.Config{
		Endpoint:      Endpoint,
		ConsumerGroup: ConsumerGroup,
		Credentials: &credentials.SessionCredentials{
			AccessKey:    AccessKey,
			AccessSecret: SecretKey,
		},
	},
		rmq_client.WithAwaitDuration(awaitDuration),
		rmq_client.WithSubscriptionExpressions(map[string]*rmq_client.FilterExpression{
			Topic: rmq_client.NewFilterExpressionWithType(
				"a IS NOT NULL AND c IS NOT NULL AND a = 'b' AND c = 'd'", rmq_client.SQL92),
		}),
	)
	if err != nil {
		log.Fatal(err)
	}
	// start simpleConsumer
	err = simpleConsumer.Start()
	if err != nil {
		log.Fatal(err)
	}
	// graceful stop simpleConsumer
	defer func() {
		if r := recover(); r != nil {
			fmt.Println(r)
		}
		_ = simpleConsumer.GracefulStop()
	}()

	fmt.Println("start receive message")

	// Each Receive call will only select one broker queue to pop messages.
	// Enable multiple consumption goroutines to reduce message end-to-end latency.
	ch := make(chan struct{})
	wg := &sync.WaitGroup{}
	for i := 0; i < receiveConcurrency; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for {
				select {
				case <-ch:
					return
				default:
					mvs, err := simpleConsumer.Receive(context.TODO(), maxMessageNum, invisibleDuration)
					if err != nil {
						fmt.Println("receive message error: " + err.Error())
					}
					// ack message
					for _, mv := range mvs {
						fmt.Println(mv)
						if err := simpleConsumer.Ack(context.TODO(), mv); err != nil {
							fmt.Println("ack message error: " + err.Error())
						}
					}
				}
			}
		}()
	}

	exit := make(chan os.Signal, 1)
	signal.Notify(exit, syscall.SIGINT, syscall.SIGTERM)

	// wait for exit
	<-exit
	close(ch)
	wg.Wait()
}
