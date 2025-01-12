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
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class ChatController {
    // FXML注入的UI组件
    @FXML
    private TextArea userInfo;      // 用户信息显示区域
    @FXML
    private TextArea chatHistory;   // 聊天历史记录显示区域
    @FXML
    private TextField messageInput; // 消息输入框
    @FXML
    private ToggleButton recordButton; // 录音按钮
    @FXML
    private HBox additionalButtons;  // 附加功能按钮容器
    @FXML
    private Button addButton;       // 展开/收起附加功能的按钮
    @FXML
    private WebView chatwebView;    // Web视图组件，用于显示云端数据

    @FXML
    public void initialize() {
        // 初始化用户信息显示
        userInfo.setText(MqttClientUtil.getClientInfo());
        
        // 设置消息输入框的回车键监听
        messageInput.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> onSendMessage();
            }
        });
        
        // 初始化Web视图，添加空值检查
        if (chatwebView != null) {
            chatwebView.getEngine().load("http://iot.arorms.cn:8080/");
        } else {
            System.err.println("WebView初始化失败！");
        }
    }

    @FXML
    public void onSendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            if (containsIllegalCharacters(message)) {
                showAlert("请不要输入非法字符：{}[]''\"\"/\\");
                return;
            }

            // 构建MQTT消息格式: {"username":"设备名","type":"txt","content":"消息内容"}
            String jsonMessage = "{\"username\":\""+MqttClientUtil.deviceName+"\",\"type\":\"txt\",\"content\":\"" + message + "\"}";
            // 发送消息到指定主题: /产品密钥/设备名/user/update
            MqttClientUtil.sendMessage("/"+MqttClientUtil.produceKey+"/"+MqttClientUtil.deviceName+"/user/update", jsonMessage);
            chatHistory.appendText("我: " + message + "\n");
            DatabaseUtil.saveMessage(message);
            messageInput.clear();
        }
    }

    // 使用正则表达式检查是否包含JSON特殊字符，防止消息格式错误
    private boolean containsIllegalCharacters(String message) {
        return message.matches(".*[\\{\\}\\[\\]''\"\"/\\\\].*");
    }

    /**
     * 显示警告对话框
     * @param message 警告信息
     */
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

    /**
     * 显示附加功能按钮的动画效果
     */
    private void showAdditionalButtons() {
        // 创建平移动画：从下方滑入
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), additionalButtons);
        slideIn.setFromY(additionalButtons.getHeight());  // 从控件高度的位置开始
        slideIn.setToY(0);  // 移动到原始位置

        // 创建淡入动画：从透明到不透明
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), additionalButtons);
        fadeIn.setFromValue(0);  // 完全透明
        fadeIn.setToValue(1);    // 完全不透明

        // 组合两个动画：先平移后淡入
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