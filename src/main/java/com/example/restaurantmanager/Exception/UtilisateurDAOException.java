package com.example.restaurantmanager.Exception;

public class UtilisateurDAOException extends DAOException {
    public UtilisateurDAOException(String message) {
        super(message);
    }
    public UtilisateurDAOException(String message, Throwable cause) {
        super(message, cause);
    }
}