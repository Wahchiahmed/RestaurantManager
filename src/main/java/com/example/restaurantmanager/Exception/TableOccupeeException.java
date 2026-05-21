package com.example.restaurantmanager.Exception;

public class TableOccupeeException extends MetierException {
    public TableOccupeeException(int numeroTable) {
        super("La table " + numeroTable + " est déjà occupée.");
    }
}