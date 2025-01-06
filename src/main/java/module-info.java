module multi.data.community {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.hivemq.client.mqtt;

    opens com.example.multidatacommunity to javafx.fxml;
    exports com.example.multidatacommunity;
}