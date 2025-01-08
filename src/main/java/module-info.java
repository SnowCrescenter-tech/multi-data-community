module multi.data.community {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.hivemq.client.mqtt;
    requires javafx.swing;
    requires org.bytedeco.ffmpeg;
    requires ffmpeg;
    requires java.sql;

    opens com.example.multidatacommunity to javafx.fxml;
    exports com.example.multidatacommunity;
}