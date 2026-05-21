package com.example.restaurantmanager.Exception;

public class ConnexionException extends DAOException {
    public ConnexionException(Throwable cause) {
        super("Impossible de se connecter à la base de données.", cause);
    }
}