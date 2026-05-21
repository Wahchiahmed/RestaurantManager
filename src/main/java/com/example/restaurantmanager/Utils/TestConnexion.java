package com.example.restaurantmanager.Utils;

import com.example.restaurantmanager.Exception.ConnexionException;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnexion {
    public static void main(String[] args) {
        try (Connection connexion = DatabaseConnection.getConnection()) {

            if (connexion != null) {
                System.out.println("Succès : Connexion à la base de données 'restaurant_db' réussie !");
            } else {
                System.out.println("Échec : La connexion a retourné null.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur : Impossible de se connecter à la base de données.");
            System.err.println("Détail de l'erreur : " + e.getMessage());
            e.printStackTrace();
        } catch (ConnexionException e) {
            throw new RuntimeException(e);
        }
    }
}