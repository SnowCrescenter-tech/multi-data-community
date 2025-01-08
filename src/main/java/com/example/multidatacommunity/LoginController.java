package com.example.multidatacommunity;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;

public class LoginController {
    @FXML
    private ComboBox<String> userSelector;
    @FXML
    private TextField clientIdField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField hostUrlField;
    @FXML
    private CheckBox rememberMeCheckBox;
    @FXML
    private Label errorLabel;

    private static final String PROPERTIES_FILE = "user.properties";

    @FXML
    public void initialize() {
        userSelector.getItems().addAll("用户A");
        userSelector.setValue("用户A");
        loadLoginDetails();
    }

    @FXML
    public void onLogin() {
        String selectedUser = userSelector.getValue();
        String clientId = clientIdField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String hostUrl = hostUrlField.getText();

        if (selectedUser == null || clientId.isEmpty() || username.isEmpty() || password.isEmpty() || hostUrl.isEmpty()) {
            errorLabel.setText("请填写所有字段！");
            return;
        }

        if (rememberMeCheckBox.isSelected()) {
            saveLoginDetails(selectedUser, clientId, username, password, hostUrl);
        }

        boolean connected = MqttClientUtil.connect(clientId, username, password, hostUrl);
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

    private void saveLoginDetails(String user, String clientId, String username, String password, String hostUrl) {
        try (OutputStream output = new FileOutputStream(PROPERTIES_FILE)) {
            Properties prop = new Properties();
            prop.setProperty("user", user);
            prop.setProperty("clientId", clientId);
            prop.setProperty("username", username);
            prop.setProperty("password", password);
            prop.setProperty("hostUrl", hostUrl);
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void loadLoginDetails() {
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            userSelector.setValue(prop.getProperty("user"));
            clientIdField.setText(prop.getProperty("clientId"));
            usernameField.setText(prop.getProperty("username"));
            passwordField.setText(prop.getProperty("password"));
            hostUrlField.setText(prop.getProperty("hostUrl"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}