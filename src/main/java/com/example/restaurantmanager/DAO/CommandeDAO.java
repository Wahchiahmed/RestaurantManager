package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.Exception.CommandeDAOException;
import com.example.restaurantmanager.Exception.CommandeVideException;
import com.example.restaurantmanager.Exception.ConnexionException;
import com.example.restaurantmanager.Model.Commande;
import com.example.restaurantmanager.Model.LigneCommande;
import com.example.restaurantmanager.Utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CommandeDAO {


    public void sauvegarderCommande(Commande commande, int idTableBaseDeDonnees)
            throws CommandeVideException, ConnexionException, CommandeDAOException {

        if (commande.getLignes() == null || commande.getLignes().isEmpty()) {
            throw new CommandeVideException();
        }

        final String sqlCommande =
                "INSERT INTO commandes (table_id, total_addition) VALUES (?, ?)";
        final String sqlLigne =
                "INSERT INTO ligne_commande (commande_id, article_id, quantite) VALUES (?, ?, ?)";

        try (Connection connexion = DatabaseConnection.getConnection()) {

            connexion.setAutoCommit(false);

            try {
                int commandeId = insererEnteteCommande(
                        connexion, sqlCommande, idTableBaseDeDonnees, commande);

                insererLignesCommande(connexion, sqlLigne, commandeId, commande);

                connexion.commit();

            } catch (SQLException ex) {
                annulerTransaction(connexion);
                throw new CommandeDAOException(
                        "Erreur SQL lors de la sauvegarde de la commande " +
                                "(table n°" + idTableBaseDeDonnees + ").", ex);
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (CommandeDAOException e) {
            throw e;

        } catch (SQLException e) {
            throw new CommandeDAOException(
                    "Erreur SQL inattendue sur la commande (table n°"
                            + idTableBaseDeDonnees + ").", e);
        }
    }


    private int insererEnteteCommande(Connection connexion,
                                      String sql,
                                      int idTable,
                                      Commande commande)
            throws SQLException, CommandeDAOException {

        try (PreparedStatement req = connexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            req.setInt(1, idTable);
            req.setDouble(2, commande.getTotalAddition());
            req.executeUpdate();

            try (ResultSet cles = req.getGeneratedKeys()) {
                if (cles.next()) {
                    return cles.getInt(1);
                }
            }
        }

        throw new CommandeDAOException(
                "Aucun ID généré après l'insertion de la commande pour la table n°"
                        + idTable + ".");
    }


    private void insererLignesCommande(Connection connexion,
                                       String sql,
                                       int commandeId,
                                       Commande commande) throws SQLException {

        try (PreparedStatement req = connexion.prepareStatement(sql)) {
            for (LigneCommande ligne : commande.getLignes()) {
                req.setInt(1, commandeId);
                req.setInt(2, ligne.getArticle().getId());
                req.setInt(3, ligne.getQuantite());
                req.addBatch(); // plus efficace qu'un executeUpdate() par ligne
            }
            req.executeBatch();
        }
    }

    private void annulerTransaction(Connection connexion) {
        try {
            connexion.rollback();
        } catch (SQLException rollbackEx) {
            System.err.println("Échec du rollback : " + rollbackEx.getMessage());
        }
    }
}