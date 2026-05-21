package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.Exception.ArticleDAOException;
import com.example.restaurantmanager.Exception.ArticleInvalideException;
import com.example.restaurantmanager.Exception.ConnexionException;
import com.example.restaurantmanager.Utils.DatabaseConnection;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class GerantDAO {


    public double getRecetteJour() throws ConnexionException, ArticleDAOException {
        return queryDouble(
                "SELECT COALESCE(SUM(total_addition), 0) FROM commandes " +
                        "WHERE DATE(date_commande) = CURDATE()"
        );
    }


    public int getNbCommandesJour() throws ConnexionException, ArticleDAOException {
        return (int) queryDouble(
                "SELECT COUNT(*) FROM commandes WHERE DATE(date_commande) = CURDATE()"
        );
    }


    public double getTicketMoyenJour() throws ConnexionException, ArticleDAOException {
        return queryDouble(
                "SELECT COALESCE(AVG(total_addition), 0) FROM commandes " +
                        "WHERE DATE(date_commande) = CURDATE()"
        );
    }


    public int getNbTablesOccupees() throws ConnexionException, ArticleDAOException {
        return (int) queryDouble(
                "SELECT COUNT(*) FROM tables_restaurant WHERE statut = 'OCCUPEE'"
        );
    }


    public double getRecetteMois() throws ConnexionException, ArticleDAOException {
        return queryDouble(
                "SELECT COALESCE(SUM(total_addition), 0) FROM commandes " +
                        "WHERE YEAR(date_commande) = YEAR(NOW()) " +
                        "AND MONTH(date_commande) = MONTH(NOW())"
        );
    }


    public Map<String, Double> getRecetteSemaine()
            throws ConnexionException, ArticleDAOException {

        Map<String, Double> resultats = new LinkedHashMap<>();
        String sql =
                "SELECT DATE(date_commande) AS jour, " +
                        "       COALESCE(SUM(total_addition), 0) AS total " +
                        "FROM commandes " +
                        "WHERE date_commande >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                        "GROUP BY DATE(date_commande) " +
                        "ORDER BY jour ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                resultats.put(rs.getString("jour"), rs.getDouble("total"));
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors du calcul de la recette sur 7 jours.", e);
        }

        return resultats;
    }


    public Map<String, Integer> getTopArticles()
            throws ConnexionException, ArticleDAOException {

        Map<String, Integer> resultats = new LinkedHashMap<>();
        String sql =
                "SELECT a.nom, SUM(lc.quantite) AS total_vendu " +
                        "FROM ligne_commande lc " +
                        "JOIN articles a ON lc.article_id = a.id " +
                        "GROUP BY a.id, a.nom " +
                        "ORDER BY total_vendu DESC " +
                        "LIMIT 5";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                resultats.put(rs.getString("nom"), rs.getInt("total_vendu"));
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors de la récupération du top 5 des articles.", e);
        }

        return resultats;
    }


    public void ajouterArticle(String nom, String type, double prix, int stock)
            throws ArticleInvalideException, ConnexionException, ArticleDAOException {

        // Validation métier
        validerArticle(nom, prix, stock);

        String sql =
                "INSERT INTO articles (nom, type, prix, quantite_stock) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom.trim());
            ps.setString(2, type);
            ps.setDouble(3, prix);
            ps.setInt(4, stock);

            int lignesAffectees = ps.executeUpdate();
            if (lignesAffectees == 0) {
                throw new ArticleDAOException(
                        "L'ajout de l'article « " + nom + " » n'a affecté aucune ligne.");
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors de l'ajout de l'article « " + nom + " ».", e);
        }
    }


    public void modifierArticle(int id, String nom, String type, double prix, int stock)
            throws ArticleInvalideException, ConnexionException, ArticleDAOException {

        validerArticle(nom, prix, stock);

        String sql =
                "UPDATE articles SET nom = ?, type = ?, prix = ?, quantite_stock = ? " +
                        "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom.trim());
            ps.setString(2, type);
            ps.setDouble(3, prix);
            ps.setInt(4, stock);
            ps.setInt(5, id);

            int lignesAffectees = ps.executeUpdate();
            if (lignesAffectees == 0) {
                throw new ArticleDAOException(
                        "Aucun article trouvé avec l'id=" + id + " à modifier.");
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors de la modification de l'article id=" + id + ".", e);
        }
    }


    public void supprimerArticle(int id)
            throws ConnexionException, ArticleDAOException {

        String sql = "DELETE FROM articles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            int lignesAffectees = ps.executeUpdate();
            if (lignesAffectees == 0) {
                throw new ArticleDAOException(
                        "Aucun article trouvé avec l'id=" + id + " à supprimer.");
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors de la suppression de l'article id=" + id + ".", e);
        }
    }


    private double queryDouble(String sql)
            throws ConnexionException, ArticleDAOException {

        try (Connection conn = DatabaseConnection.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {

            if (rs.next()) return rs.getDouble(1);

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new ArticleDAOException(
                    "Erreur SQL lors de l'exécution de la requête : " + sql, e);
        }

        return 0;
    }


    private void validerArticle(String nom, double prix, int stock)
            throws ArticleInvalideException {

        if (nom == null || nom.trim().isEmpty()) {
            throw new ArticleInvalideException(
                    "Le nom de l'article est obligatoire.");
        }
        if (prix < 0) {
            throw new ArticleInvalideException(
                    "Le prix de l'article ne peut pas être négatif (reçu : " + prix + ").");
        }
        if (stock < 0) {
            throw new ArticleInvalideException(
                    "Le stock de l'article ne peut pas être négatif (reçu : " + stock + ").");
        }
    }
}