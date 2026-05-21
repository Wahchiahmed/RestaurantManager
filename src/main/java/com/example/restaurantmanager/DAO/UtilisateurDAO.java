package com.example.restaurantmanager.DAO;

import com.example.restaurantmanager.Exception.CompteDesactiveException;
import com.example.restaurantmanager.Exception.ConnexionException;
import com.example.restaurantmanager.Exception.LoginInvalideException;
import com.example.restaurantmanager.Exception.UtilisateurDAOException;
import com.example.restaurantmanager.Model.Utilisateur;
import com.example.restaurantmanager.Utils.DatabaseConnection;
import com.example.restaurantmanager.Utils.HashUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {


    public Utilisateur authentifier(String login, String motDePasse)
            throws LoginInvalideException,
            CompteDesactiveException,
            ConnexionException,
            UtilisateurDAOException {


        String sqlLogin = "SELECT * FROM users WHERE login = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlLogin)) {

            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new LoginInvalideException();
                }

                Utilisateur u = mapRow(rs);

                String hashSaisi = HashUtil.sha256(motDePasse);
                if (!hashSaisi.equals(u.getMotDePasse())) {
                    throw new LoginInvalideException();

                }

                if (!u.isActif()) {
                    throw new CompteDesactiveException(login);
                }

                mettreAJourDerniereConnexion(u.getId(), conn);
                return u;
            }

        } catch (ConnexionException | LoginInvalideException | CompteDesactiveException e) {
            throw e;

        } catch (SQLException e) {
            throw new UtilisateurDAOException(
                    "Erreur SQL lors de l'authentification du login « "
                            + login + " ».", e);
        }
    }


    public List<Utilisateur> getAllUtilisateurs()
            throws ConnexionException, UtilisateurDAOException {

        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, nom";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(mapRow(rs));
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new UtilisateurDAOException(
                    "Erreur SQL lors du chargement des utilisateurs.", e);
        }

        return liste;
    }


    public void creerUtilisateur(String nom,
                                 String login,
                                 String motDePasse,
                                 Utilisateur.Role role)
            throws ConnexionException, UtilisateurDAOException {

        String sql =
                "INSERT INTO users (nom, login, mot_de_passe, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, login);
            ps.setString(3, HashUtil.sha256(motDePasse));
            ps.setString(4, role.name());

            int lignesAffectees = ps.executeUpdate();
            if (lignesAffectees == 0) {
                throw new UtilisateurDAOException(
                        "La création du compte « " + login
                                + " » n'a affecté aucune ligne.");
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new UtilisateurDAOException(
                        "Le login « " + login + " » est déjà utilisé.", e);
            }
            throw new UtilisateurDAOException(
                    "Erreur SQL lors de la création du compte « " + login + " ».", e);
        }
    }


    public void toggleActif(int id, boolean actif)
            throws ConnexionException, UtilisateurDAOException {

        String sql = "UPDATE users SET actif = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, actif ? 1 : 0);
            ps.setInt(2, id);

            int lignesAffectees = ps.executeUpdate();
            if (lignesAffectees == 0) {
                throw new UtilisateurDAOException(
                        "Aucun utilisateur trouvé avec l'id=" + id
                                + " pour modifier son statut.");
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new UtilisateurDAOException(
                    "Erreur SQL lors du changement de statut (id=" + id + ").", e);
        }
    }


    public void supprimerUtilisateur(int id)
            throws ConnexionException, UtilisateurDAOException {

        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            int lignesAffectees = ps.executeUpdate();
            if (lignesAffectees == 0) {
                throw new UtilisateurDAOException(
                        "Aucun utilisateur trouvé avec l'id=" + id + " à supprimer.");
            }

        } catch (ConnexionException e) {
            throw e;

        } catch (SQLException e) {
            throw new UtilisateurDAOException(
                    "Erreur SQL lors de la suppression de l'utilisateur id="
                            + id + ".", e);
        }
    }


    private void mettreAJourDerniereConnexion(int id, Connection conn)
            throws SQLException {

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