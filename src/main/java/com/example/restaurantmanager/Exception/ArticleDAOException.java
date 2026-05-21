package com.example.restaurantmanager.Exception;

public class ArticleDAOException extends DAOException {
    public ArticleDAOException(String message, Throwable cause) { super(message, cause); }
    public ArticleDAOException(String message) { super(message); }
}