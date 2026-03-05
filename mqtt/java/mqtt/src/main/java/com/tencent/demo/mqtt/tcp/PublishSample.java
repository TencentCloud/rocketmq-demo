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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tencent.demo.mqtt.tcp;

import com.tencent.demo.mqtt.Utils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PublishSample {

    // You can get the broker address from tencent cloud console.
    private static final String BROKER_ADDR = "sample.mqtt.public.tencenttdmq.com:1883";

    // You need to create a pair of username and password at tencent cloud console.
    private static final String USERNAME = "qcloud";
    private static final String PASSWORD = "sample";

    // You need to create the first topic at tencent cloud console before starting your app.
    private static final String FIRST_TOPIC = "home";

    // Please Make sure the clientId is unique.
    private static final String CLIENT_ID = MqttClient.generateClientId();

    private static final boolean CLEAN_SESSION = true;
    private static final int CONNECT_TIMEOUT = 3000;
    private static final int REQUEST_TIMEOUT = 3000;

    public static void main(String[] args) {
        MqttClient client = null;
        try {
            String server = "tcp://" + BROKER_ADDR;
            // Non-volatile form of persistence should be used if CLEAN_SESSION is set to false.
            client = new MqttClient(server, CLIENT_ID, new MemoryPersistence());
            client.setCallback(Utils.buildMqttCallback());
            // Set the maximum time to wait for a request to complete.
            client.setTimeToWait(REQUEST_TIMEOUT);
            MqttConnectOptions options = Utils.buildMqttConnectOptions(USERNAME, PASSWORD, CLEAN_SESSION, CONNECT_TIMEOUT);

            client.connect(options);
            if (!client.isConnected()) {
                Utils.println("Failed to connect to broker: " + server);
                return;
            } else {
                Utils.println("Connected to broker: " + server);
            }

            for (int i = 0; i < 10; i++) {
                client.publish(FIRST_TOPIC + "/humidity/room0", Utils.buildMqttMessage("40%", 1, false));
                client.publish(FIRST_TOPIC + "/humidity/room1", Utils.buildMqttMessage("60%", 1, false));
            }

            client.disconnect();
            client.close();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (client != null) {
                try {
                    if (client.isConnected()) {
                        client.disconnect();
                    }
                    client.close();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
