package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.DAO.ArticleDAO;
import com.example.restaurantmanager.Exception.ArticleDAOException;
import com.example.restaurantmanager.Exception.ArticleInvalideException;
import com.example.restaurantmanager.Exception.ConnexionException;
import com.example.restaurantmanager.Model.Article;
import java.util.List;

public class TestDAO {
    public static void main(String[] args) throws ConnexionException, ArticleDAOException, ArticleInvalideException {
        ArticleDAO articleDAO = new ArticleDAO();
        List<Article> menu = articleDAO.getAllArticles();

        System.out.println("--- Menu du Restaurant ---");
        for (Article a : menu) {
            // L'appel à getDescription() va exécuter la méthode spécifique de Plat ou de Boisson
            System.out.println(a.getDescription() + " | Stock : " + a.getQuantiteStock());
        }
    }
}