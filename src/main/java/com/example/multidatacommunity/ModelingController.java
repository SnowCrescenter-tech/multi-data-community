package com.example.multidatacommunity;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.io.File;
import java.util.Random;

public class ModelingController {
    @FXML
    private ComboBox<String> modelSelector;
    @FXML
    private TextField parameterField1;
    @FXML
    private TextField parameterField2;
    @FXML
    private LineChart<Number, Number> chart;
    @FXML
    private Button generateButton;
    @FXML
    private Button sendButton;

    private File generatedImage;

    @FXML
    public void initialize() {
        modelSelector.getItems().addAll("随机数", "正态分布", "泊松分布");
        modelSelector.setOnAction(event -> updateParameterFields());
    }

    private void updateParameterFields() {
        String model = modelSelector.getValue();
        if ("正态分布".equals(model)) {
            parameterField1.setPromptText("均值 (μ)");
            parameterField2.setPromptText("标准差 (σ)");
            parameterField2.setVisible(true);
        } else if ("泊松分布".equals(model)) {
            parameterField1.setPromptText("λ");
            parameterField2.setVisible(false);
        } else {
            parameterField1.setPromptText("参数");
            parameterField2.setVisible(false);
        }
    }

    @FXML
    public void onGenerate() {
        String model = modelSelector.getValue();
        String parameter1 = parameterField1.getText();
        String parameter2 = parameterField2.getText();

        if (model == null || parameter1.isEmpty() || ("正态分布".equals(model) && parameter2.isEmpty())) {
            System.out.println("请选择模型并填写参数！");
            return;
        }

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        Random random = new Random();

        switch (model) {
            case "随机数" -> {
                for (int i = 0; i < 100; i++) {
                    series.getData().add(new XYChart.Data<>(i, random.nextDouble() * Double.parseDouble(parameter1)));
                }
            }
            case "正态分布" -> {
                double mean = Double.parseDouble(parameter1);
                double stdDev = Double.parseDouble(parameter2);
                for (int i = 0; i < 100; i++) {
                    series.getData().add(new XYChart.Data<>(i, mean + stdDev * random.nextGaussian()));
                }
            }
            case "泊松分布" -> {
                double lambda = Double.parseDouble(parameter1);
                for (int i = 0; i < 100; i++) {
                    series.getData().add(new XYChart.Data<>(i, -Math.log(1.0 - random.nextDouble()) / lambda));
                }
            }
        }

        chart.getData().clear();
        chart.getData().add(series);

        // 将图表保存为图像
        generatedImage = new File("temp/model.png");
        // 假设有保存图像的功能实现
    }

    @FXML
    public void onSend() {
        if (generatedImage != null) {
            String encodedData = ImageUtil.encodeToBase64(generatedImage);
            com.example.multidatacommunity.MqttClientUtil.sendMessage("chat/topic", "image:" + encodedData);
        }
    }
}