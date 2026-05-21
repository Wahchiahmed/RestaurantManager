package com.example.restaurantmanager.Exception;

public class CommandeDAOException extends DAOException {
    public CommandeDAOException(String message, Throwable cause) { super(message, cause);}
        public CommandeDAOException(String message) { super(message); }
}