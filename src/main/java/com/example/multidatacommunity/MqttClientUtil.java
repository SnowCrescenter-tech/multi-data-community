package com.example.multidatacommunity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MqttClientUtil {
    private static Mqtt3AsyncClient client;
    public static String produceKey;
    public static String deviceName;

    public static boolean connect(String clientId, String username, String password, String hostUrl) {
        try {
            client = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier(clientId)
                    .serverHost(hostUrl)
                    .serverPort(1883)
                    .simpleAuth()
                    .username(username)
                    .password(password.getBytes())
                    .applySimpleAuth()
                    .buildAsync();

            client.connectWith()
                    .cleanSession(true)
                    .send()
                    .whenComplete((ack, throwable) -> {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        } else {
                            System.out.println("Connected successfully");
                        }
                    });

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void sendMessage(String topic, String message) {
        try {
            if (client.getState() == MqttClientState.CONNECTED) {
                String jsonMessage = message;
                client.publishWith()
                        .topic(topic)
                        .payload(jsonMessage.getBytes())
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .send()
                        .whenComplete((publish, throwable) -> {
                            if (throwable != null) {
                                throwable.printStackTrace();
                            } else {
                                System.out.println("Message sent successfully");
                            }
                        });
            } else {
                System.out.println("Client is not connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getClientInfo() {
        String info;
        String tempinfo;

        tempinfo = client.getConfig().getClientIdentifier().toString();
        Pattern pattern = Pattern.compile("Optional\\[(\\w+)\\.(\\w+)\\|");
        Matcher matcher = pattern.matcher(tempinfo);

            if (matcher.find()) {
                produceKey = matcher.group(1);
            deviceName = matcher.group(2);
            System.out.println("Produce Key: " + produceKey);
            System.out.println("Device Name: " + deviceName);
        } else {
            System.out.println("No match found");
        }
        info =
            "产品密钥：" + produceKey +
            "\n当前设备名称: " + deviceName +
            "\n目前与云端的连接状态: " + ((client.getState()).toString().equals("CONNECTING")?"已连接":"未连接")
        ;
        return info;
//                client.getConfig().getClientIdentifier().orElse(MqttClientIdentifier.of("Unknown")) +
//                "\nConnected: " + (client.getState());
    }
}