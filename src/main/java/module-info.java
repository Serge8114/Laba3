module com.example.laba3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.laba3 to javafx.fxml;
    exports com.example.laba3;
}