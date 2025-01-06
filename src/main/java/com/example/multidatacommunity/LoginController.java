package com.example.multidatacommunity;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private ComboBox<String> userSelector;
    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        userSelector.getItems().addAll("用户A", "用户B", "用户C");
    }

    @FXML
    public void onLogin() {
        String selectedUser = userSelector.getValue();
        if (selectedUser == null) {
            errorLabel.setText("请选择用户！");
            return;
        }

        boolean connected = com.example.multidatacommunity.MqttClientUtil.connect(selectedUser);
        if (connected) {
            Stage stage = (Stage) userSelector.getScene().getWindow();
            stage.close();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/multidatacommunity/chat.fxml"));
                Stage chatStage = new Stage();
                chatStage.setScene(new Scene(loader.load()));
                chatStage.setTitle("聊天界面");
                chatStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("连接失败，请检查网络！");
        }
    }
}
