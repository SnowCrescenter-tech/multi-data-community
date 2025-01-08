package com.example.multidatacommunity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

public class MqttClientUtil {
    private static Mqtt3AsyncClient client;

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

        tempinfo = String.valueOf(client.getConfig().getClientIdentifier());
        info = tempinfo;
        return info;
//                client.getConfig().getClientIdentifier().orElse(MqttClientIdentifier.of("Unknown")) +
//                "\nConnected: " + (client.getState());
    }
}