package com.example.restaurantmanager.DAO;

// N'oublie pas d'importer LigneCommande au lieu de Article
import com.example.restaurantmanager.Model.LigneCommande;
import com.example.restaurantmanager.Model.Commande;
import com.example.restaurantmanager.Utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class CommandeDAO {

    public boolean sauvegarderCommande(Commande commande, int idTableBaseDeDonnees) {
        String sqlCommande = "INSERT INTO commandes (table_id, total_addition) VALUES (?, ?)";
        String sqlLigne = "INSERT INTO ligne_commande (commande_id, article_id, quantite) VALUES (?, ?, ?)";

        try (Connection connexion = DatabaseConnection.getConnection()) {

            // 1. Désactiver l'auto-commit pour gérer la transaction manuellement
            connexion.setAutoCommit(false);

            try (PreparedStatement reqCommande = connexion.prepareStatement(sqlCommande, Statement.RETURN_GENERATED_KEYS)) {

                // 2. Insérer l'entête de la commande
                reqCommande.setInt(1, idTableBaseDeDonnees);
                reqCommande.setDouble(2, commande.getTotalAddition());
                reqCommande.executeUpdate();

                // 3. Récupérer l'ID de la commande généré par MySQL
                ResultSet clesGenerees = reqCommande.getGeneratedKeys();
                int commandeId = -1;
                if (clesGenerees.next()) {
                    commandeId = clesGenerees.getInt(1);
                }

                // 4. Insérer chaque ligne dans ligne_commande (MISE À JOUR ICI)
                if (commandeId != -1) {
                    try (PreparedStatement reqLigne = connexion.prepareStatement(sqlLigne)) {

                        // On boucle maintenant sur getLignes() et on manipule des objets LigneCommande
                        for (LigneCommande ligne : commande.getLignes()) {
                            reqLigne.setInt(1, commandeId);
                            reqLigne.setInt(2, ligne.getArticle().getId()); // L'ID de l'article lié à cette ligne
                            reqLigne.setInt(3, ligne.getQuantite());        // La VRAIE quantité cumulée !
                            reqLigne.executeUpdate();
                        }
                    }
                }

                // 5. Valider la transaction si tout s'est bien passé
                connexion.commit();
                return true;

            } catch (SQLException ex) {
                // En cas d'erreur, on annule tout (Rollback)
                connexion.rollback();
                System.err.println("Transaction annulée suite à une erreur : " + ex.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Erreur de connexion lors de la sauvegarde : " + e.getMessage());
            return false;
        }
    }
}