package com.example.multidatacommunity;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ModelingUtil {
    /**
     * 显示数学建模窗口
     * - 加载modeling.fxml界面
     * - 设置为模态窗口
     */
    public static void showModelingWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(ModelingUtil.class.getResource("/com/example/multidatacommunity/modeling.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("数学建模");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);  // 设置为模态窗口
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}