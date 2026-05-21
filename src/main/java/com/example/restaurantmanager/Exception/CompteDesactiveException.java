package com.example.restaurantmanager.Exception;

public class CompteDesactiveException extends AuthException {
    public CompteDesactiveException(String login) {
        super("Le compte « " + login + " » est désactivé.");
    }
}