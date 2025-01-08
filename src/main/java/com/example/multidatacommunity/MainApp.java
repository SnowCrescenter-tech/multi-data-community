package com.example.multidatacommunity;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
public void start(Stage primaryStage) throws Exception {
    DatabaseUtil.initializeDatabase();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/multidatacommunity/login.fxml"));
    Scene scene = new Scene(loader.load(), 400, 300);
    primaryStage.setScene(scene);
    primaryStage.setTitle("物联网信息交流云平台");
    primaryStage.show();
}

    public static void main(String[] args) {
        launch(args);
    }
}
//正常发送