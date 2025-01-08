package com.example.multidatacommunity;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Button;
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
    private ToggleButton recordButton;

    @FXML
    public void initialize() {
        userInfo.setText(MqttClientUtil.getClientInfo());
        messageInput.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> onSendMessage();
            }
        });
    }

    @FXML
    public void onSendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            String jsonMessage = "{\" username\":\"sender\",\"type\":\"txt\",\"content\":\"" + message + "\"}";
            MqttClientUtil.sendMessage("/k176bpRR9g2/user_one/user/update", jsonMessage);
            chatHistory.appendText("我: " + message + "\n");
            DatabaseUtil.saveMessage(message);
            messageInput.clear();
        }
    }

    @FXML
    public void onRecordAudio() {
        if (recordButton.isSelected()) {
            RecordingUtil.startRecording();
            recordButton.setText("停止录音");
            chatHistory.appendText("开始录音...\n");
        } else {
            RecordingUtil.stopRecording();
            recordButton.setText("录音");
            chatHistory.appendText("录音结束: [语音]\n");
        }
    }

    @FXML
    public void onSendAudio() {
        File audioFile = new File("temp/recording.mp3");
        if (audioFile.exists()) {
            String encodedAudio = RecordingUtil.encodeToBase64();
            String jsonMessage = "{\"type\":\"record\",\"content\":\"" + encodedAudio + "\"}";
            MqttClientUtil.sendMessage("/k176bpRR9g2/user_one/user/update", jsonMessage);
            DatabaseUtil.saveAudio(encodedAudio);

            Button audioButton = new Button("播放语音");
            audioButton.setOnAction(event -> playAudio(encodedAudio));
            chatHistory.appendText("我: ");
            chatHistory.appendText(audioButton.getText() + "\n");
        }
    }

    private void playAudio(String base64Audio) {
        RecordingUtil.playBase64Audio(base64Audio);
    }

    @FXML
    public void onSendImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            String encodedImage = ImageUtil.encodeToBase64(selectedFile);
            String jsonMessage = "{\"type\":\"image\",\"content\":\"" + encodedImage + "\"}";
            MqttClientUtil.sendMessage("/k176bpRR9g2/user_one/user/update", jsonMessage);
        }
    }

    @FXML
    public void onModeling() {
        ModelingUtil.showModelingWindow();
    }

    @FXML
    public void onAddOption() {
        // Implement the functionality for the "+" button here
        System.out.println("Add option button clicked");
    }
}