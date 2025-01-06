package com.example.multidatacommunity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

public class MqttClientUtil {
    private static Mqtt3AsyncClient client;

    public static boolean connect(String userId) {
        try {
            client = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier(userId)
                    .serverHost("broker.hivemq.com")
                    .serverPort(1883)
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
                client.publishWith()
                        .topic(topic)
                        .payload(message.getBytes())
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
        return "ClientID: " + client.getConfig().getClientIdentifier().orElse(MqttClientIdentifier.of("Unknown")) +
                "\nConnected: " + (client.getState() == MqttClientState.CONNECTED);
    }
}