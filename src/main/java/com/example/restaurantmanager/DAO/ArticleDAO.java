package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.Exception.ArticleDAOException;
import com.example.restaurantmanager.Exception.ArticleInvalideException;
import com.example.restaurantmanager.Exception.ConnexionException;
import com.example.restaurantmanager.Model.*;
import com.example.restaurantmanager.Utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {


    public List<Article> getAllArticles()
            throws ConnexionException, ArticleDAOException, ArticleInvalideException {

        List<Article> listeArticles = new ArrayList<>();
        String sql = "SELECT * FROM articles";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql);
             ResultSet resultat = requete.executeQuery()) {

            while (resultat.next()) {
                int    id            = resultat.getInt("id");
                String nom           = resultat.getString("nom");
                String type          = resultat.getString("type");
                double prix          = resultat.getDouble("prix");
                int    quantiteStock = resultat.getInt("quantite_stock");

                Article article = creerArticle(id, nom, type, prix, quantiteStock);
                listeArticles.add(article);
            }

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors de la récupération des articles.", e
            );
        }

        return listeArticles;
    }


    public Article getArticleById(int id)
            throws ConnexionException, ArticleDAOException, ArticleInvalideException {

        String sql = "SELECT * FROM articles WHERE id = ?";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql)) {

            requete.setInt(1, id);

            try (ResultSet resultat = requete.executeQuery()) {
                if (resultat.next()) {
                    String nom           = resultat.getString("nom");
                    String type          = resultat.getString("type");
                    double prix          = resultat.getDouble("prix");
                    int    quantiteStock = resultat.getInt("quantite_stock");

                    return creerArticle(id, nom, type, prix, quantiteStock);
                }
            }

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors de la récupération de l'article id=" + id, e
            );
        }

        return null; // article introuvable
    }

    public void mettreAJourStock(int idArticle, int nouvelleQuantite)
            throws ConnexionException, ArticleDAOException {

        String sql = "UPDATE articles SET quantite_stock = ? WHERE id = ?";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql)) {

            requete.setInt(1, nouvelleQuantite);
            requete.setInt(2, idArticle);
            requete.executeUpdate();

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors de la mise à jour du stock de l'article id=" + idArticle, e
            );
        }
    }

    private Article creerArticle(int id, String nom, String type, double prix, int quantiteStock)
            throws ArticleInvalideException {

        switch (type.toUpperCase()) {
            case "PLAT":    return new Plat(id, nom, prix, quantiteStock);
            case "BOISSON": return new Boisson(id, nom, prix, quantiteStock);
            case "ENTREE":  return new Entree(id, nom, prix, quantiteStock);
            case "DESSERT": return new Dessert(id, nom, prix, quantiteStock);
            default:
                throw new ArticleInvalideException(
                        "Type d'article inconnu en base de données : « " + type + " »"
                );
        }
    }
}