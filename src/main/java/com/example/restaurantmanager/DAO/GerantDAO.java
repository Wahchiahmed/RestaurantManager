package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.Utils.DatabaseConnection;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class GerantDAO {

    // ── Dashboard ─────────────────────────────────────────────────────────────

    /** Recette totale du jour (commandes validées aujourd'hui) */
    public double getRecetteJour() {
        String sql = "SELECT COALESCE(SUM(total_addition), 0) FROM commandes " +
                "WHERE DATE(date_commande) = CURDATE()";
        return queryDouble(sql);
    }

    /** Nombre de commandes du jour */
    public int getNbCommandesJour() {
        String sql = "SELECT COUNT(*) FROM commandes WHERE DATE(date_commande) = CURDATE()";
        return (int) queryDouble(sql);
    }

    /** Ticket moyen du jour */
    public double getTicketMoyenJour() {
        String sql = "SELECT COALESCE(AVG(total_addition), 0) FROM commandes " +
                "WHERE DATE(date_commande) = CURDATE()";
        return queryDouble(sql);
    }

    /** Nombre de tables actuellement occupées */
    public int getNbTablesOccupees() {
        String sql = "SELECT COUNT(*) FROM tables_restaurant WHERE statut = 'OCCUPEE'";
        return (int) queryDouble(sql);
    }

    /** Recette des 7 derniers jours (date → montant) */
    public Map<String, Double> getRecetteSemaine() {
        Map<String, Double> resultats = new LinkedHashMap<>();
        String sql = "SELECT DATE(date_commande) AS jour, COALESCE(SUM(total_addition), 0) AS total " +
                "FROM commandes " +
                "WHERE date_commande >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "GROUP BY DATE(date_commande) " +
                "ORDER BY jour ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                resultats.put(rs.getString("jour"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recette semaine : " + e.getMessage());
        }
        return resultats;
    }

    /** Top 5 articles les plus vendus (toutes périodes) */
    public Map<String, Integer> getTopArticles() {
        Map<String, Integer> resultats = new LinkedHashMap<>();
        String sql = "SELECT a.nom, SUM(lc.quantite) AS total_vendu " +
                "FROM ligne_commande lc " +
                "JOIN articles a ON lc.article_id = a.id " +
                "GROUP BY a.id, a.nom " +
                "ORDER BY total_vendu DESC " +
                "LIMIT 5";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                resultats.put(rs.getString("nom"), rs.getInt("total_vendu"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur top articles : " + e.getMessage());
        }
        return resultats;
    }

    /** Recette totale du mois en cours */
    public double getRecetteMois() {
        String sql = "SELECT COALESCE(SUM(total_addition), 0) FROM commandes " +
                "WHERE YEAR(date_commande) = YEAR(NOW()) AND MONTH(date_commande) = MONTH(NOW())";
        return queryDouble(sql);
    }

    // ── Gestion Articles ──────────────────────────────────────────────────────

    public boolean ajouterArticle(String nom, String type, double prix, int stock) {
        String sql = "INSERT INTO articles (nom, type, prix, quantite_stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, type);
            ps.setDouble(3, prix);
            ps.setInt(4, stock);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur ajout article : " + e.getMessage());
            return false;
        }
    }

    public boolean modifierArticle(int id, String nom, String type, double prix, int stock) {
        String sql = "UPDATE articles SET nom = ?, type = ?, prix = ?, quantite_stock = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, type);
            ps.setDouble(3, prix);
            ps.setInt(4, stock);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification article : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimerArticle(int id) {
        String sql = "DELETE FROM articles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression article : " + e.getMessage());
            return false;
        }
    }

    // ── Utilitaire ────────────────────────────────────────────────────────────
    private double queryDouble(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Erreur requête : " + e.getMessage());
        }
        return 0;
    }
}