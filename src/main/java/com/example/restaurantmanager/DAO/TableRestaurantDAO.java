package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.Exception.ConnexionException;
import com.example.restaurantmanager.Exception.TableDAOException;
import com.example.restaurantmanager.Exception.TableOccupeeException;
import com.example.restaurantmanager.Model.TableRestaurant;
import com.example.restaurantmanager.Utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableRestaurantDAO {


    public List<TableRestaurant> getAllTables()
            throws ConnexionException, TableDAOException {

        List<TableRestaurant> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables_restaurant";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql);
             ResultSet resultat = requete.executeQuery()) {

            while (resultat.next()) {
                TableRestaurant table = new TableRestaurant(
                        resultat.getInt("numero_table"));
                table.setStatut(resultat.getString("statut"));
                tables.add(table);
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new TableDAOException(
                    "Erreur SQL lors de la récupération des tables.", e);
        }

        return tables;
    }


    public void updateStatut(int numeroTable, String nouveauStatut)
            throws TableOccupeeException, ConnexionException, TableDAOException {

        // Vérification métier : éviter de double-occuper une table
        if ("OCCUPEE".equals(nouveauStatut)) {
            TableRestaurant table = getTableByNumero(numeroTable);
            if (table != null && "OCCUPEE".equals(table.getStatut())) {
                throw new TableOccupeeException(numeroTable);
            }
        }

        String sql = "UPDATE tables_restaurant SET statut = ? WHERE numero_table = ?";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql)) {

            requete.setString(1, nouveauStatut);
            requete.setInt(2, numeroTable);

            int lignesAffectees = requete.executeUpdate();
            if (lignesAffectees == 0) {
                throw new TableDAOException(
                        "Aucune table trouvée avec le numéro " + numeroTable + ".");
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new TableDAOException(
                    "Erreur SQL lors de la mise à jour du statut de la table n°"
                            + numeroTable + ".", e);
        }
    }

    private TableRestaurant getTableByNumero(int numeroTable)
            throws ConnexionException, TableDAOException {

        String sql = "SELECT * FROM tables_restaurant WHERE numero_table = ?";

        try (Connection connexion = DatabaseConnection.getConnection();
             PreparedStatement requete = connexion.prepareStatement(sql)) {

            requete.setInt(1, numeroTable);

            try (ResultSet resultat = requete.executeQuery()) {
                if (resultat.next()) {
                    TableRestaurant table = new TableRestaurant(
                            resultat.getInt("numero_table"));
                    table.setStatut(resultat.getString("statut"));
                    return table;
                }
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new TableDAOException(
                    "Erreur SQL lors de la recherche de la table n°"
                            + numeroTable + ".", e);
        }

        return null; // table introuvable : cas normal
    }
}