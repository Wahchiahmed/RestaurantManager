package com.example.restaurantmanager.Utils;

import com.example.restaurantmanager.Exception.ConnexionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_database";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws ConnexionException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new ConnexionException(e);
        }
    }
}