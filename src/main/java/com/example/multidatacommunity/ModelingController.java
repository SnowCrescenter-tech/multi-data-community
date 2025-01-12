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

    // FXML注入的UI组件
    @FXML
    private ComboBox<String> modelSelector;    // 模型选择下拉框
    @FXML
    private TextField parameterField1;         // 参数输入框1
    @FXML
    private TextField parameterField2;         // 参数输入框2
    @FXML
    private TextField parameterField3;         // 参数输入框3
    @FXML
    private Label parameterLabel1;            // 参数标签1
    @FXML
    private Label parameterLabel2;            // 参数标签2
    @FXML
    private Label parameterLabel3;            // 参数标签3
    @FXML
    private LineChart<Number, Number> chart;  // 数据图表
    @FXML
    private Button generateButton;            // 生成按钮
    @FXML
    private Button sendButton;                // 发送按钮

    private File generatedImage;              // 生成的图表图像文件

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

    /**
     * 生成随机数分布
     * 实现原理：
     * 1. 通过循环生成指定数量的数据点
     * 2. 每个数据点的值在最小值和最大值之间随机生成
     * 3. X轴表示节点索引，Y轴表示随机生成的值
     */
    private void generateRandomNumbers(XYChart.Series<Number, Number> series, int nodeCount, double minValue, double maxValue, Random random) {
        // 生成指定数量的随机数据点
        for (int i = 0; i < nodeCount; i++) {
            series.getData().add(new XYChart.Data<>(i, minValue + (maxValue - minValue) * random.nextDouble()));
        }
    }

    /**
     * 生成正态分布
     * 实现原理：
     * 1. 使用Box-Muller变换生成标准正态分布(μ=0, σ²=1)
     * 2. 通过线性变换调整到目标均值和方差
     * 3. 使用HashMap统计每个值出现的频率
     * 4. X轴表示取值，Y轴表示频率
     */
    private void generateNormalDistribution(XYChart.Series<Number, Number> series, double mean, double variance, Random random) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        // 计算标准差
        double stdDev = Math.sqrt(variance);
        // 使用Box-Muller变换生成正态分布
        for (int i = 0; i < 1000; i++) {
            // random.nextGaussian()生成均值为0、方差为1的标准正态分布
            // 通过线性变换得到指定均值和方差的正态分布
            int value = (int) Math.round(mean + stdDev * random.nextGaussian());
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }
        // 将频率数据添加到图表序列中
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * 生成泊松分布
     * 实现原理：
     * 1. 使用接受-拒绝采样算法生成泊松分布
     * 2. exp(-lambda)作为接受阈值
     * 3. 通过多次随机采样直到满足条件
     * 4. 使用HashMap统计频率分布
     * 
     * @param lambda 泊松分布的参数λ，表示单位时间内平均发生次数
     */
    private void generatePoissonDistribution(XYChart.Series<Number, Number> series, double lambda, Random random) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        // 使用接受-拒绝采样法生成泊松分布样本
        for (int i = 0; i < 1000; i++) {
            int k = 0;
            double p = 1.0;
            // exp(-lambda)是泊松分布的归一化常数
            double L = Math.exp(-lambda);
            // 使用拒绝采样方法：当随机概率大于阈值时继续采样
            while (p > L) {
                k++;
                p *= random.nextDouble(); // 生成(0,1)之间的随机数
            }
            // k-1为最终的泊松分布随机数
            int value = k - 1;
            // 使用Map统计各个值出现的频率
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }
        
        // 将频率数据转换为图表数据点
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
        // 将图表转换为JavaFX图像
        WritableImage image = chart.snapshot(null, null);
        File file = new File("temp/model.png");
        try {
            // 使用SwingFXUtils将JavaFX图像转换为可写入文件的格式
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