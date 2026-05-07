package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.Model.Utilisateur;
import com.example.restaurantmanager.Utils.DatabaseConnection;
import com.example.restaurantmanager.Utils.HashUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    // ── Authentification ──────────────────────────────────────────────────────
    public Utilisateur authentifier(String login, String motDePasse) {
        String hash = HashUtil.sha256(motDePasse);
        String sql  = "SELECT * FROM users WHERE login = ? AND mot_de_passe = ? AND actif = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, hash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utilisateur u = mapRow(rs);
                    mettreAJourDerniereConnexion(u.getId(), conn);
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur authentification : " + e.getMessage());
        }
        return null;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────
    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, nom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st   = conn.createStatement();
             ResultSet rs   = st.executeQuery(sql)) {

            while (rs.next()) liste.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Erreur chargement utilisateurs : " + e.getMessage());
        }
        return liste;
    }

    public boolean creerUtilisateur(String nom, String login, String motDePasse, Utilisateur.Role role) {
        String sql = "INSERT INTO users (nom, login, mot_de_passe, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, login);
            ps.setString(3, HashUtil.sha256(motDePasse));
            ps.setString(4, role.name());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur création utilisateur : " + e.getMessage());
            return false;
        }
    }

    public boolean toggleActif(int id, boolean actif) {
        String sql = "UPDATE users SET actif = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, actif ? 1 : 0);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur toggle actif : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimerUtilisateur(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression utilisateur : " + e.getMessage());
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void mettreAJourDerniereConnexion(int id, Connection conn) throws SQLException {
        String sql = "UPDATE users SET derniere_connexion = NOW() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setLogin(rs.getString("login"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
        u.setActif(rs.getInt("actif") == 1);

        Timestamp dc = rs.getTimestamp("date_creation");
        if (dc != null) u.setDateCreation(dc.toLocalDateTime());

        Timestamp dl = rs.getTimestamp("derniere_connexion");
        if (dl != null) u.setDerniereConnexion(dl.toLocalDateTime());

        return u;
    }
}