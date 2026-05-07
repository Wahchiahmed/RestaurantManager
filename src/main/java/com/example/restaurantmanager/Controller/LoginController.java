package com.example.restaurantmanager.Controller;

import com.example.restaurantmanager.DAO.UtilisateurDAO;
import com.example.restaurantmanager.Model.Utilisateur;
import com.example.restaurantmanager.Utils.Session;
import javafx.animation.FadeTransition;
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

    @FXML
    public void initialize() {
        labelErreur.setVisible(false);
        // Permettre connexion avec Entrée
        champMotDePasse.setOnAction(e -> seConnecter());
        champLogin.setOnAction(e -> champMotDePasse.requestFocus());
    }

    @FXML
    void seConnecter() {
        String login = champLogin.getText().trim();
        String mdp   = champMotDePasse.getText();

        if (login.isEmpty() || mdp.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        btnConnexion.setDisable(true);
        btnConnexion.setText("Connexion...");

        Utilisateur u = utilisateurDAO.authentifier(login, mdp);

        if (u == null) {
            afficherErreur("Identifiants incorrects ou compte désactivé.");
            btnConnexion.setDisable(false);
            btnConnexion.setText("Se connecter");
            agiterChamp();
            return;
        }

        // Stocker la session
        Session.getInstance().setUtilisateur(u);

        // Rediriger selon le rôle
        try {
            String fxml = u.isGerant() ? "/com/example/restaurantmanager/gerant-view.fxml"
                    : "/com/example/restaurantmanager/MainView.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) btnConnexion.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/restaurantmanager/styles.css").toExternalForm());

            // Animation de transition
            root.setOpacity(0);
            stage.setScene(scene);
            if (u.isGerant()) {
                stage.setWidth(1200);
                stage.setHeight(750);
            } else {
                stage.setWidth(1050);
                stage.setHeight(680);
            }
            stage.centerOnScreen();

            FadeTransition ft = new FadeTransition(Duration.millis(400), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        }  catch (Exception e) {
        // 1. Cette ligne imprime l'erreur complète dans la console IntelliJ
        e.printStackTrace();

        // 2. Affichage sur l'interface graphique
        afficherErreur("Erreur de chargement de l'interface : " + e.getMessage());

        // 3. Réactivation du bouton
        btnConnexion.setDisable(false);
        btnConnexion.setText("Se connecter");
    }
    }

    private void afficherErreur(String message) {
        labelErreur.setText(message);
        labelErreur.setVisible(true);
    }

    private void agiterChamp() {
        // Animation shake sur le panneau login
        javafx.animation.TranslateTransition shake =
                new javafx.animation.TranslateTransition(Duration.millis(60), panneauLogin);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }
}