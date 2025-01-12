package com.example.multidatacommunity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttClientIdentifier;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MqttClientUtil {
    // MQTT客户端相关变量
    private static Mqtt3AsyncClient client;  // MQTT异步客户端实例
    public static String produceKey;         // 产品密钥
    public static String deviceName;         // 设备名称

    /**
     * 连接MQTT服务器
     * @param clientId 客户端标识
     * @param username 用户名
     * @param password 密码
     * @param hostUrl 服务器地址
     * @return 连接是否成功
     */
    public static boolean connect(String clientId, String username, String password, String hostUrl) {
        try {
            // 配置MQTT客户端
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

            // 建立连接
            client.connectWith()
                    .cleanSession(true)
                    .send()
                    .whenComplete((ack, throwable) -> {
                        if (throwable != null) {
                            throwable.printStackTrace();
                        } else {
                            System.out.println("连接成功");
                        }
                    });

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送MQTT消息
     * @param topic 主题
     * @param message JSON格式的消息内容
     */
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

        // 获取客户端标识符信息
        tempinfo = client.getConfig().getClientIdentifier().toString();
        // 使用正则表达式解析clientId格式：Optional[产品密钥.设备名|xxx]
        Pattern pattern = Pattern.compile("Optional\\[(\\w+)\\.(\\w+)\\|");
        Matcher matcher = pattern.matcher(tempinfo);

        if (matcher.find()) {
            // 提取产品密钥和设备名
            produceKey = matcher.group(1);  // 第一个捕获组是产品密钥
            deviceName = matcher.group(2);  // 第二个捕获组是设备名
            System.out.println("Produce Key: " + produceKey);
            System.out.println("Device Name: " + deviceName);
        } else {
            System.out.println("No match found");
        }

        // 构建显示信息，包含连接状态判断
        info =
            "产品密钥：" + produceKey +
            "\n当前设备名称: " + deviceName +
            "\n目前与云端的连接状态: " + ((client.getState()).toString().equals("CONNECTED")?"已连接":"未连接")
        ;
        return info;
    }
}