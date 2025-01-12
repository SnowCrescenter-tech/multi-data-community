package com.example.multidatacommunity;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 初始化数据库
        DatabaseUtil.initializeDatabase();
        
        // 加载登录界面
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/multidatacommunity/login.fxml"));
        Scene scene = new Scene(loader.load(), 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("物联网信息数据流转云平台");
        primaryStage.show();
    }

    // 程序入口
    public static void main(String[] args) {
        launch(args);
    }
}