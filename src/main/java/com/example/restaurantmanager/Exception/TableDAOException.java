package com.example.restaurantmanager.Exception;

public class TableDAOException extends DAOException {
    public TableDAOException(String message) {
        super(message);
    }
    public TableDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}