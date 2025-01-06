package com.example.multidatacommunity;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ChatController {
    @FXML
    private TextArea userInfo;
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextField messageInput;

    @FXML
    public void initialize() {
        userInfo.setText(com.example.multidatacommunity.MqttClientUtil.getClientInfo());
    }

    @FXML
    public void onSendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            String jsonMessage = "{\"type\":\"txt\",\"content\":\"" + message + "\"}";
            com.example.multidatacommunity.MqttClientUtil.sendMessage("chat/topic", jsonMessage);
            chatHistory.appendText("我: " + message + "\n");
            messageInput.clear();
        }
    }

    @FXML
    public void onAddOption() {
        // 显示录音、发送图片和数学建模功能
        // 这里可以实现一个滑出界面，包含录音、发送图片和数学建模的按钮
    }

    @FXML
    public void onRecordAudio() {
        // 实现录音功能
        File audioFile = RecordingUtil.recordAudio();
        if (audioFile != null) {
            chatHistory.appendText("我: [语音]\n");
        }
    }

    @FXML
    public void onSendAudio() {
        // 发送录音
        File audioFile = new File("temp/recording.wav");
        if (audioFile.exists()) {
            String encodedAudio = RecordingUtil.encodeToBase64(audioFile);
            String jsonMessage = "{\"type\":\"record\",\"content\":\"" + encodedAudio + "\"}";
            com.example.multidatacommunity.MqttClientUtil.sendMessage("chat/topic", jsonMessage);
        }
    }

    @FXML
    public void onSendImage() {
        // 发送图片
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            String encodedImage = ImageUtil.encodeToBase64(selectedFile);
            String jsonMessage = "{\"type\":\"image\",\"content\":\"" + encodedImage + "\"}";
            com.example.multidatacommunity.MqttClientUtil.sendMessage("chat/topic", jsonMessage);
        }
    }

    @FXML
    public void onModeling() {
        // 显示数学建模界面
        com.example.multidatacommunity.ModelingUtil.showModelingWindow();
    }
}