module com.example.restaurantmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires kernel;
    requires layout;


    opens com.example.restaurantmanager to javafx.fxml;
    exports com.example.restaurantmanager;

    // Ajoute ces deux lignes pour autoriser JavaFX à utiliser ton contrôleur
    opens com.example.restaurantmanager.Controller to javafx.fxml;
    exports com.example.restaurantmanager.Controller;

    // Si tu as des erreurs futures avec TableView qui n'affiche pas tes articles, ajoute aussi :
    opens com.example.restaurantmanager.Model to javafx.base;
}