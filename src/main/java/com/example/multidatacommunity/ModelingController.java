package com.example.multidatacommunity;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ModelingController {

    @FXML
    private ComboBox<String> modelSelector;
    @FXML
    private TextField parameterField1;
    @FXML
    private TextField parameterField2;
    @FXML
    private TextField parameterField3;
    @FXML
    private Label parameterLabel1;
    @FXML
    private Label parameterLabel2;
    @FXML
    private Label parameterLabel3;
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
        updateParameterFields();
    }

    private void updateParameterFields() {
        String model = modelSelector.getValue();
        parameterField1.setVisible(false);
        parameterField2.setVisible(false);
        parameterField3.setVisible(false);
        parameterLabel1.setVisible(false);
        parameterLabel2.setVisible(false);
        parameterLabel3.setVisible(false);

        if ("随机数".equals(model)) {
            parameterLabel1.setText("节点数量");
            parameterLabel2.setText("节点最小值");
            parameterLabel3.setText("节点最大值");
            parameterField1.setVisible(true);
            parameterField2.setVisible(true);
            parameterField3.setVisible(true);
            parameterLabel1.setVisible(true);
            parameterLabel2.setVisible(true);
            parameterLabel3.setVisible(true);
        } else if ("正态分布".equals(model)) {
            parameterLabel1.setText("均值 (μ)");
            parameterLabel2.setText("方差 (σ²)");
            parameterField1.setVisible(true);
            parameterField2.setVisible(true);
            parameterLabel1.setVisible(true);
            parameterLabel2.setVisible(true);
        } else if ("泊松分布".equals(model)) {
            parameterLabel1.setText("平均次数 (λ)");
            parameterField1.setVisible(true);
            parameterLabel1.setVisible(true);
        }
    }

    @FXML
    public void onGenerate() {
        String model = modelSelector.getValue();
        String parameter1 = parameterField1.getText();
        String parameter2 = parameterField2.getText();
        String parameter3 = parameterField3.getText();

        if (model == null || parameter1.isEmpty() ||
                ("正态分布".equals(model) && parameter2.isEmpty()) ||
                ("随机数".equals(model) && (parameter2.isEmpty() || parameter3.isEmpty()))) {
            System.out.println("请选择模型并填写参数！");
            return;
        }

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        Random random = new Random();

        switch (model) {
            case "随机数" -> generateRandomNumbers(series, Integer.parseInt(parameter1), Double.parseDouble(parameter2), Double.parseDouble(parameter3), random);
            case "正态分布" -> generateNormalDistribution(series, Double.parseDouble(parameter1), Double.parseDouble(parameter2), random);
            case "泊松分布" -> generatePoissonDistribution(series, Double.parseDouble(parameter1), random);
        }

        chart.getData().clear();
        chart.getData().add(series);

        setChartTitleAndLabels(model);

        series.getNode().setStyle("-fx-stroke: blue; -fx-stroke-width: 2;");

        saveChartAsImage();
    }

    private void generateRandomNumbers(XYChart.Series<Number, Number> series, int nodeCount, double minValue, double maxValue, Random random) {
        for (int i = 0; i < nodeCount; i++) {
            series.getData().add(new XYChart.Data<>(i, minValue + (maxValue - minValue) * random.nextDouble()));
        }
    }

    private void generateNormalDistribution(XYChart.Series<Number, Number> series, double mean, double variance, Random random) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        double stdDev = Math.sqrt(variance);
        for (int i = 0; i < 1000; i++) {
            int value = (int) Math.round(mean + stdDev * random.nextGaussian());
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
    }

    private void generatePoissonDistribution(XYChart.Series<Number, Number> series, double lambda, Random random) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            int k = 0;
            double p = 1.0;
            double L = Math.exp(-lambda);
            while (p > L) {
                k++;
                p *= random.nextDouble();
            }
            int value = k - 1;
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
    }

    private void setChartTitleAndLabels(String model) {
        switch (model) {
            case "随机数":
                chart.setTitle("随机数分布");
                chart.getXAxis().setLabel("节点索引");
                chart.getYAxis().setLabel("节点值");
                break;
            case "正态分布":
                chart.setTitle("正态分布");
                chart.getXAxis().setLabel("值");
                chart.getYAxis().setLabel("次数");
                break;
            case "泊松分布":
                chart.setTitle("泊松分布");
                chart.getXAxis().setLabel("值");
                chart.getYAxis().setLabel("次数");
                break;
        }
    }

    private void saveChartAsImage() {
        WritableImage image = chart.snapshot(null, null);
        File file = new File("temp/model.png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            generatedImage = file;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSend() {
        saveChartAsImage(); // 确保在发送之前保存图片
        if (generatedImage != null && generatedImage.exists()) {
            String encodedData = ImageUtil.encodeToBase64(generatedImage);
            MqttClientUtil.sendMessage("/"+MqttClientUtil.produceKey+"/"+MqttClientUtil.deviceName+"/user/update", "{\"username\":\"sender\",\"type\":\"model\",\"content\":\"" + encodedData + "\"}");
        } else {
            System.out.println("生成的图像文件不存在");
        }
    }
}