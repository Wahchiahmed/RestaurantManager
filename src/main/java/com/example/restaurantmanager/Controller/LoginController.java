package com.example.restaurantmanager.Controller;

import com.example.restaurantmanager.DAO.UtilisateurDAO;
import com.example.restaurantmanager.Exception.CompteDesactiveException;
import com.example.restaurantmanager.Exception.ConnexionException;
import com.example.restaurantmanager.Exception.LoginInvalideException;
import com.example.restaurantmanager.Exception.UtilisateurDAOException;
import com.example.restaurantmanager.Model.Utilisateur;
import com.example.restaurantmanager.Utils.Session;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField     champLogin;
    @FXML private PasswordField champMotDePasse;
    @FXML private Label         labelErreur;
    @FXML private Button        btnConnexion;
    @FXML private VBox          panneauLogin;

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    // =========================================================================
    // INITIALISATION
    // =========================================================================

    @FXML
    public void initialize() {
        labelErreur.setVisible(false);
        champLogin.setOnAction(e -> champMotDePasse.requestFocus());
        champMotDePasse.setOnAction(e -> seConnecter());
    }

    // =========================================================================
    // CONNEXION
    // =========================================================================

    @FXML
    void seConnecter() {
        String login = champLogin.getText().trim();
        String mdp   = champMotDePasse.getText();

        // ── Validation des champs vides ────────────────────────────────────
        if (login.isEmpty() || mdp.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            agiterChamp();
            return;
        }

        // ── Désactivation du bouton pendant la tentative ───────────────────
        btnConnexion.setDisable(true);
        btnConnexion.setText("Connexion...");
        labelErreur.setVisible(false);

        try {
            // ── Tentative d'authentification ───────────────────────────────
            Utilisateur u = utilisateurDAO.authentifier(login, mdp);

            // Succès : stocker la session et naviguer
            Session.getInstance().setUtilisateur(u);
            naviguerVersInterface(u);

        } catch (LoginInvalideException e) {
            // Login introuvable ou mot de passe incorrect
            afficherErreur("Identifiants incorrects.");
            reinitialiserBouton();
            agiterChamp();
            champMotDePasse.clear();
            champMotDePasse.requestFocus();

        } catch (CompteDesactiveException e) {
            // Le compte existe mais est désactivé
            afficherErreur(e.getMessage());
            reinitialiserBouton();
            agiterChamp();

        } catch (ConnexionException e) {
            // La BDD est inaccessible
            afficherErreur("Impossible de joindre le serveur. Réessayez plus tard.");
            reinitialiserBouton();

        } catch (UtilisateurDAOException e) {
            // Erreur SQL inattendue
            afficherErreur("Erreur technique lors de la connexion.");
            reinitialiserBouton();
            // Log pour le développeur
            System.err.println("[LoginController] Erreur DAO : " + e.getMessage());
        }
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================

    /**
     * Redirige l'utilisateur vers son interface selon son rôle,
     * avec une animation de fondu.
     */
    private void naviguerVersInterface(Utilisateur u) {
        try {
            String fxml = u.isGerant()
                    ? "/com/example/restaurantmanager/gerant-view.fxml"
                    : "/com/example/restaurantmanager/MainView.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) btnConnexion.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass()
                    .getResource("/com/example/restaurantmanager/styles.css")
                    .toExternalForm());

            // Taille de la fenêtre selon le rôle
            if (u.isGerant()) {
                stage.setWidth(1200);
                stage.setHeight(750);
            } else {
                stage.setWidth(1050);
                stage.setHeight(680);
            }
            stage.centerOnScreen();

            // Animation de fondu
            root.setOpacity(0);
            stage.setScene(scene);

            FadeTransition ft = new FadeTransition(Duration.millis(400), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            // Erreur de chargement FXML (ne devrait pas arriver en production)
            e.printStackTrace();
            afficherErreur("Erreur de chargement de l'interface : " + e.getMessage());
            reinitialiserBouton();
        }
    }

    // =========================================================================
    // UTILITAIRES UI
    // =========================================================================

    /**
     * Affiche un message d'erreur en rouge sous le formulaire.
     */
    private void afficherErreur(String message) {
        labelErreur.setText("✗  " + message);
        labelErreur.setVisible(true);
    }

    /**
     * Réactive le bouton de connexion après un échec.
     */
    private void reinitialiserBouton() {
        btnConnexion.setDisable(false);
        btnConnexion.setText("Se connecter");
    }

    /**
     * Animation "shake" sur le panneau de login pour signaler une erreur.
     */
    private void agiterChamp() {
        TranslateTransition shake =
                new TranslateTransition(Duration.millis(60), panneauLogin);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }
}