module com.example.restaurantmanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.restaurantmanager to javafx.fxml;
    exports com.example.restaurantmanager;
}