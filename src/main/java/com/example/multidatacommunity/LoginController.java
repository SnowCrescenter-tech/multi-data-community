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
    // FXML注入的UI组件
    @FXML
    private ComboBox<String> userSelector;    // 用户选择下拉框
    @FXML
    private TextField clientIdField;          // 客户端ID输入框
    @FXML
    private TextField usernameField;          // 用户名输入框
    @FXML
    private PasswordField passwordField;      // 密码输入框
    @FXML
    private TextField hostUrlField;           // 服务器地址输入框
    @FXML
    private CheckBox rememberMeCheckBox;      // 记住登录信息复选框
    @FXML
    private Label errorLabel;                 // 错误信息显示标签

    // 用户配置文件路径
    private static final String PROPERTIES_FILE = "user.properties";

    @FXML
    public void initialize() {
        // 初始化用户选择下拉框
        userSelector.getItems().addAll("用户A");
        userSelector.setValue("用户A");
        loadLoginDetails();  // 加载保存的登录信息
    }

    /**
     * 处理登录按钮点击事件
     * - 验证输入字段
     * - 保存登录信息（如果选中记住我）
     * - 连接MQTT服务器
     * - 打开主聊天窗口
     */
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
                chatStage.setTitle("信息界面");
                chatStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("连接失败，请检查网络！");
        }
    }

    /**
     * 保存登录信息到配置文件
     */
    private void saveLoginDetails(String user, String clientId, String username, String password, String hostUrl) {
        try (OutputStream output = new FileOutputStream(PROPERTIES_FILE)) {
            // 使用Properties类保存配置信息
            Properties prop = new Properties();
            // 存储各项登录信息
            prop.setProperty("user", user);           // 用户类型
            prop.setProperty("clientId", clientId);   // MQTT客户端ID
            prop.setProperty("username", username);    // MQTT用户名
            prop.setProperty("password", password);    // MQTT密码
            prop.setProperty("hostUrl", hostUrl);     // MQTT服务器地址
            // 写入配置文件（null表示不添加注释）
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * 从配置文件加载登录信息
     */
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