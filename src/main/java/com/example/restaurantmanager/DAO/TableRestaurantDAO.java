package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.Model.TableRestaurant;
import com.example.restaurantmanager.Utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableRestaurantDAO {

    // Récupérer toutes les tables
    public List<TableRestaurant> getAllTables() {
        List<TableRestaurant> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables_restaurant";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql);
             ResultSet resultat = requete.executeQuery()) {

            while (resultat.next()) {
                TableRestaurant table = new TableRestaurant(resultat.getInt("numero_table"));
                table.setStatut(resultat.getString("statut"));
                tables.add(table);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des tables : " + e.getMessage());
        }
        return tables;
    }

    // Mettre à jour le statut d'une table
    public void updateStatut(int numeroTable, String nouveauStatut) {
        String sql = "UPDATE tables_restaurant SET statut = ? WHERE numero_table = ?";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql)) {

            requete.setString(1, nouveauStatut);
            requete.setInt(2, numeroTable);
            requete.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la table : " + e.getMessage());
        }
    }
}