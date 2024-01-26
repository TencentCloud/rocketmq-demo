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

package com.tencent.demo.mqtt;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Utils {

    private static final String DEFAULT_PASSWORD = "";

    public static MqttCallback buildMqttCallback() {
        return new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                println(MessageFormat.format("connection lost, cause: {0}", cause));
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Add codes here to process receive message.
                println(MessageFormat.format("received message from topic: {0}, payload: {1}", topic, message.toString()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    println(MessageFormat.format("delivered message to topics: {0}", Arrays.asList(token.getTopics())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static MqttConnectOptions buildMqttConnectOptions(String username, String password, boolean cleanSession,
        int connectTimeout) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectTimeout);
        return options;
    }

    public static MqttConnectOptions buildMqttConnectOptions(String username, String password, boolean cleanSession,
        int connectTimeout, String caCertPath, String clientCertPath, String clientKeyPath) throws Exception {
        MqttConnectOptions options = buildMqttConnectOptions(username, password, cleanSession, connectTimeout);
        options.setSocketFactory(getSocketFactory(caCertPath, clientCertPath, clientKeyPath));
        return options;
    }

    public static MqttConnectOptions buildMqttConnectOptions(String username, String password, boolean cleanSession,
        int connectTimeout, String caCertPath) throws Exception {
        MqttConnectOptions options = buildMqttConnectOptions(username, password, cleanSession, connectTimeout);
        options.setSocketFactory(getSingleSocketFactory(caCertPath));
        return options;
    }

    public static MqttMessage buildMqttMessage(String payload, int qos, boolean retained) {
        MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        msg.setQos(qos);
        msg.setRetained(retained);
        return msg;
    }

    public static SSLSocketFactory getSingleSocketFactory(final String caCrtFile) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, DEFAULT_PASSWORD.toCharArray());
        try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get(caCrtFile)))) {
            int certIndex = 1;
            while (stream.available() > 0) {
                String alias = "ca-certificate-" + certIndex++;
                caKs.setCertificateEntry(alias, cf.generateCertificate(stream));
            }
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }

    public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile)
        throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, DEFAULT_PASSWORD.toCharArray());
        try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get(caCrtFile)))) {
            int certIndex = 1;
            while (stream.available() > 0) {
                String alias = "ca-certificate-" + certIndex++;
                caKs.setCertificateEntry(alias, cf.generateCertificate(stream));
            }
        }

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, DEFAULT_PASSWORD.toCharArray());
        Certificate[] chain;
        try (FileInputStream stream = new FileInputStream(crtFile)) {
            Collection<? extends Certificate> certs = cf.generateCertificates(stream);
            chain = certs.toArray(new Certificate[0]);
        }
        try (PEMParser pemParser = new PEMParser(new FileReader(keyFile))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PrivateKey key = converter.getPrivateKey((PrivateKeyInfo) object);
            ks.setKeyEntry("private-key",key, DEFAULT_PASSWORD.toCharArray(), chain);
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, DEFAULT_PASSWORD.toCharArray());
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }

    public static void println(String message) {
        System.out.println(MessageFormat.format("{0}{1}", now(), message));
    }

    private static String now() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return simpleDateFormat.format(new Date()) + "\t";
    }
}
