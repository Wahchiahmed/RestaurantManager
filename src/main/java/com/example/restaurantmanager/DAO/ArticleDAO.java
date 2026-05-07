package com.example.restaurantmanager.DAO;


import com.example.restaurantmanager.Model.*;
import com.example.restaurantmanager.Utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {


    public List<Article> getAllArticles() {
        List<Article> listeArticles = new ArrayList<>();
        String sql = "SELECT * FROM articles";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql);
             ResultSet resultat = requete.executeQuery()) {

            while (resultat.next()) {
                int id = resultat.getInt("id");
                String nom = resultat.getString("nom");
                String type = resultat.getString("type");
                double prix = resultat.getDouble("prix");
                int quantiteStock = resultat.getInt("quantite_stock");


                Article article = null;

                switch (type.toUpperCase()) {
                    case "PLAT":
                        article = new Plat(id, nom, prix, quantiteStock);
                        break;
                    case "BOISSON":
                        article = new Boisson(id, nom, prix, quantiteStock);
                        break;
                    case "ENTREE":
                        article = new Entree(id, nom, prix, quantiteStock);
                        break;
                    case "DESSERT":
                        article = new Dessert(id, nom, prix, quantiteStock);
                        break;
                    default:
                        System.err.println("Type d'article inconnu : " + type);
                }

                listeArticles.add(article);
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération des articles : " + e.getMessage());
        }

        return listeArticles;
    }
}