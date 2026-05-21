package com.example.restaurantmanager.Exception;

public class StockInsuffisantException extends MetierException {
    public StockInsuffisantException(String nomArticle, int dispo, int demande) {
        super("Stock insuffisant pour « " + nomArticle
                + " » : disponible=" + dispo + ", demandé=" + demande);
    }
}