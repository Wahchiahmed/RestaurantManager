package com.example.restaurantmanager.Exception;

public class LoginInvalideException extends AuthException {
    public LoginInvalideException() {
        super("Identifiants incorrects.");
    }
}