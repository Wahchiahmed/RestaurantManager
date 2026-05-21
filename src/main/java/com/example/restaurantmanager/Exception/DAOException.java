package com.example.restaurantmanager.Exception;

public class DAOException extends RestaurantException {
    public DAOException(String message) { super(message); }
    public DAOException(String message, Throwable cause) { super(message, cause); }
}