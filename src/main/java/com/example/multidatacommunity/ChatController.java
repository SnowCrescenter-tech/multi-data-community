package com.example.multidatacommunity;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private HBox additionalButtons;
    @FXML
    private Button addButton;

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
            if (containsIllegalCharacters(message)) {
                showAlert("请不要输入非法字符：{}[]''\"\"/\\");
                return;
            }

            String jsonMessage = "{\"username\":\""+MqttClientUtil.deviceName+"\",\"type\":\"txt\",\"content\":\"" + message + "\"}";
            MqttClientUtil.sendMessage("/"+MqttClientUtil.produceKey+"/"+MqttClientUtil.deviceName+"/user/update", jsonMessage);
            chatHistory.appendText("我: " + message + "\n");
            DatabaseUtil.saveMessage(message);
            messageInput.clear();
        }
    }

    private boolean containsIllegalCharacters(String message) {
        return message.matches(".*[\\{\\}\\[\\]''\"\"/\\\\].*");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("非法字符");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            String jsonMessage = "{\"username\":\""+MqttClientUtil.deviceName+"\",\"type\":\"record\",\"content\":\"" + encodedAudio + "\"}";
            MqttClientUtil.sendMessage("/"+MqttClientUtil.produceKey+"/"+MqttClientUtil.deviceName+"/user/update", jsonMessage);
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
            String jsonMessage = "{\"username\":\""+MqttClientUtil.deviceName+"\",\"type\":\"image\",\"content\":\"" + encodedImage + "\"}";
            MqttClientUtil.sendMessage("/"+MqttClientUtil.produceKey+"/"+MqttClientUtil.deviceName+"/user/update", jsonMessage);
        }
    }

    @FXML
    public void onModeling() {
        ModelingUtil.showModelingWindow();
    }

    @FXML
    public void onAddOption() {
        boolean isVisible = additionalButtons.isVisible();
        if (isVisible) {
            hideAdditionalButtons();
            addButton.setText("+");
        } else {
            showAdditionalButtons();
            addButton.setText("-");
        }
    }

    private void showAdditionalButtons() {
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), additionalButtons);
        slideIn.setFromY(additionalButtons.getHeight());
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), additionalButtons);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        SequentialTransition transition = new SequentialTransition(slideIn, fadeIn);
        transition.setOnFinished(event -> additionalButtons.setVisible(true));
        transition.play();
    }

    private void hideAdditionalButtons() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), additionalButtons);
        slideOut.setFromY(0);
        slideOut.setToY(additionalButtons.getHeight());

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), additionalButtons);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition transition = new SequentialTransition(fadeOut, slideOut);
        transition.setOnFinished(event -> additionalButtons.setVisible(false));
        transition.play();
    }
}