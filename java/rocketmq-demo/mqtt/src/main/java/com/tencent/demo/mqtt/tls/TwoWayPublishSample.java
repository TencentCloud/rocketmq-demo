package com.tencent.demo.mqtt.tls;

import com.tencent.demo.mqtt.Utils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class TwoWayPublishSample {

    // You can get the broker address from tencent cloud console.
    private static final String BROKER_ADDR = "mqtt-sample.mqtt.public.tencenttdmq.com:8883";

    // You need to create a pair of username and password at tencent cloud console.
    private static final String USERNAME = "qcloud";
    private static final String PASSWORD = "sample";

    // You need to create the first topic at tencent cloud console before starting your app.
    private static final String FIRST_TOPIC = "home";

    // You should make sure the clientId is unique.
    private static final String CLIENT_ID = MqttClient.generateClientId();

    // You should set your certificate file path.
    private static final String CA_CERT_FILE_PATH = "ca.crt";
    private static final String CLIENT_CERT_FILE_PATH = "client.crt";
    private static final String CLIENT_KEY_FILE_PATH = "client.key";

    private static final boolean CLEAN_SESSION = true;
    private static final int CONNECT_TIMEOUT = 3000;
    private static final int REQUEST_TIMEOUT = 3000;

    public static void main(String[] args) {
        MqttClient client = null;
        try {
            String server = "ssl://" + BROKER_ADDR;
            // Non-volatile form of persistence should be used if CLEAN_SESSION is set to false.
            client = new MqttClient(server, CLIENT_ID, new MemoryPersistence());
            client.setCallback(Utils.buildMqttCallback());
            // Set the maximum time to wait for a request to complete.
            client.setTimeToWait(REQUEST_TIMEOUT);
            MqttConnectOptions options = Utils.buildMqttConnectOptions(USERNAME, PASSWORD, CLEAN_SESSION, CONNECT_TIMEOUT,
                CA_CERT_FILE_PATH, CLIENT_CERT_FILE_PATH, CLIENT_KEY_FILE_PATH);

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
