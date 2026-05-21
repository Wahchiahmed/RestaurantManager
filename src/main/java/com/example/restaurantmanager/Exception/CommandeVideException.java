package com.example.restaurantmanager.Exception;

public class CommandeVideException extends MetierException {
    public CommandeVideException() {
        super("Impossible de valider une commande vide.");
    }
}