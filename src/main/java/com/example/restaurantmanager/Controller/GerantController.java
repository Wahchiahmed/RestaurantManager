package com.example.restaurantmanager.Controller;

import com.example.restaurantmanager.DAO.ArticleDAO;
import com.example.restaurantmanager.DAO.GerantDAO;
import com.example.restaurantmanager.DAO.UtilisateurDAO;
import com.example.restaurantmanager.Model.Article;
import com.example.restaurantmanager.Model.Utilisateur;
import com.example.restaurantmanager.Utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class GerantController {

    @FXML private Label labelNomGerant;
    @FXML private Label labelRecetteJour;
    @FXML private Label labelNbCommandes;
    @FXML private Label labelTicketMoyen;
    @FXML private Label labelTablesOccupees;
    @FXML private Label labelRecetteMois;

    @FXML private VBox  conteneurTopArticles;

    @FXML private TableView<Article>           tableArticles;
    @FXML private TableColumn<Article, Integer> colId;
    @FXML private TableColumn<Article, String>  colNom;
    @FXML private TableColumn<Article, String>  colType;
    @FXML private TableColumn<Article, Double>  colPrix;
    @FXML private TableColumn<Article, Integer> colStock;

    @FXML private TextField   champNom;
    @FXML private ComboBox<String> comboType;
    @FXML private TextField   champPrix;
    @FXML private TextField   champStock;
    @FXML private Button      btnAjouter;
    @FXML private Button      btnModifier;
    @FXML private Button      btnSupprimer;
    @FXML private Label       labelFormErreur;

    @FXML private TableView<Utilisateur>              tableStaff;
    @FXML private TableColumn<Utilisateur, String>    colStaffNom;
    @FXML private TableColumn<Utilisateur, String>    colStaffLogin;
    @FXML private TableColumn<Utilisateur, String>    colStaffRole;
    @FXML private TableColumn<Utilisateur, String>    colStaffStatut;

    @FXML private TextField   champStaffNom;
    @FXML private TextField   champStaffLogin;
    @FXML private PasswordField champStaffMdp;
    @FXML private ComboBox<String> comboStaffRole;

    private final GerantDAO      gerantDAO      = new GerantDAO();
    private final ArticleDAO     articleDAO     = new ArticleDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private Article articleSelectionne = null;

    @FXML
    public void initialize() {
        Utilisateur u = Session.getInstance().getUtilisateur();
        if (u != null) labelNomGerant.setText("Bonjour, " + u.getNom());

        chargerDashboard();
        configurerTableArticles();
        chargerArticles();
        configurerTableStaff();
        chargerStaff();

        comboType.setItems(FXCollections.observableArrayList("PLAT", "BOISSON", "DESSERT", "ENTREE"));
        comboType.getSelectionModel().selectFirst();

        comboStaffRole.setItems(FXCollections.observableArrayList("CAISSIER", "GERANT"));
        comboStaffRole.getSelectionModel().selectFirst();

        tableArticles.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            articleSelectionne = sel;
            if (sel != null) {
                champNom.setText(sel.getNom());
                champPrix.setText(String.valueOf(sel.getPrix()));
                champStock.setText(String.valueOf(sel.getQuantiteStock()));
                btnModifier.setDisable(false);
                btnSupprimer.setDisable(false);
            } else {
                viderFormulaire();
            }
        });
    }

    private void chargerDashboard() {
        labelRecetteJour.setText(String.format("%.2f Dt", gerantDAO.getRecetteJour()));
        labelNbCommandes.setText(String.valueOf(gerantDAO.getNbCommandesJour()));
        labelTicketMoyen.setText(String.format("%.2f Dt", gerantDAO.getTicketMoyenJour()));
        labelTablesOccupees.setText(gerantDAO.getNbTablesOccupees() + " / 8");
        labelRecetteMois.setText(String.format("%.2f Dt", gerantDAO.getRecetteMois()));

        // Top articles
        conteneurTopArticles.getChildren().clear();
        Map<String, Integer> top = gerantDAO.getTopArticles();
        int rang = 1;
        for (Map.Entry<String, Integer> e : top.entrySet()) {
            Label l = new Label(rang + ".  " + e.getKey() + "  —  " + e.getValue() + " vendus");
            l.getStyleClass().add("top-article-item");
            conteneurTopArticles.getChildren().add(l);
            rang++;
        }
    }

    @FXML
    void actualiserDashboard() { chargerDashboard(); }

    private void configurerTableArticles() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));
    }

    private void chargerArticles() {
        List<Article> articles = articleDAO.getAllArticles();
        tableArticles.setItems(FXCollections.observableArrayList(articles));
    }

    @FXML
    void ajouterArticle() {
        if (!validerFormulaire()) return;

        boolean ok = gerantDAO.ajouterArticle(
                champNom.getText().trim(),
                comboType.getValue(),
                Double.parseDouble(champPrix.getText().trim()),
                Integer.parseInt(champStock.getText().trim())
        );

        if (ok) {
            chargerArticles();
            viderFormulaire();
            labelFormErreur.setText("✓ Article ajouté avec succès.");
            labelFormErreur.setStyle("-fx-text-fill: #2ecc71;");
            labelFormErreur.setVisible(true);
        } else {
            afficherErreurFormulaire("Erreur lors de l'ajout.");
        }
    }

    @FXML
    void modifierArticle() {
        if (articleSelectionne == null || !validerFormulaire()) return;

        boolean ok = gerantDAO.modifierArticle(
                articleSelectionne.getId(),
                champNom.getText().trim(),
                comboType.getValue(),
                Double.parseDouble(champPrix.getText().trim()),
                Integer.parseInt(champStock.getText().trim())
        );

        if (ok) {
            chargerArticles();
            viderFormulaire();
            labelFormErreur.setText("✓ Article modifié.");
            labelFormErreur.setStyle("-fx-text-fill: #2ecc71;");
            labelFormErreur.setVisible(true);
        } else {
            afficherErreurFormulaire("Erreur lors de la modification.");
        }
    }

    @FXML
    void supprimerArticle() {
        if (articleSelectionne == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer « " + articleSelectionne.getNom() + " » du menu ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = gerantDAO.supprimerArticle(articleSelectionne.getId());
                if (ok) {
                    chargerArticles();
                    viderFormulaire();
                }
            }
        });
    }

    private boolean validerFormulaire() {
        labelFormErreur.setVisible(false);
        if (champNom.getText().trim().isEmpty()) {
            afficherErreurFormulaire("Le nom est obligatoire.");
            return false;
        }
        try { Double.parseDouble(champPrix.getText().trim()); }
        catch (NumberFormatException e) { afficherErreurFormulaire("Prix invalide."); return false; }

        try { Integer.parseInt(champStock.getText().trim()); }
        catch (NumberFormatException e) { afficherErreurFormulaire("Stock invalide."); return false; }

        return true;
    }

    private void afficherErreurFormulaire(String msg) {
        labelFormErreur.setText("✗ " + msg);
        labelFormErreur.setStyle("-fx-text-fill: #e94560;");
        labelFormErreur.setVisible(true);
    }

    private void viderFormulaire() {
        champNom.clear();
        comboType.getSelectionModel().selectFirst();
        champPrix.clear();
        champStock.clear();
        tableArticles.getSelectionModel().clearSelection();
        articleSelectionne = null;
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        labelFormErreur.setVisible(false);
    }

    private void configurerTableStaff() {
        colStaffNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colStaffLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colStaffRole.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRole().name()));
        colStaffStatut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActif() ? "Actif" : "Désactivé"));
    }

    private void chargerStaff() {
        tableStaff.setItems(FXCollections.observableArrayList(utilisateurDAO.getAllUtilisateurs()));
    }

    @FXML
    void ajouterStaff() {
        String nom   = champStaffNom.getText().trim();
        String login = champStaffLogin.getText().trim();
        String mdp   = champStaffMdp.getText();
        String role  = comboStaffRole.getValue();

        if (nom.isEmpty() || login.isEmpty() || mdp.isEmpty()) {
            alerte("Erreur", "Tous les champs sont obligatoires.");
            return;
        }

        boolean ok = utilisateurDAO.creerUtilisateur(nom, login, mdp,
                Utilisateur.Role.valueOf(role));
        if (ok) {
            chargerStaff();
            champStaffNom.clear();
            champStaffLogin.clear();
            champStaffMdp.clear();
            alerte("Succès", "Compte créé avec succès.");
        } else {
            alerte("Erreur", "Login déjà utilisé ou erreur base de données.");
        }
    }

    @FXML
    void toggleActivationStaff() {
        Utilisateur sel = tableStaff.getSelectionModel().getSelectedItem();
        if (sel == null) { alerte("Avertissement", "Sélectionnez un employé."); return; }

        boolean nouvelEtat = !sel.isActif();
        if (utilisateurDAO.toggleActif(sel.getId(), nouvelEtat)) {
            chargerStaff();
        }
    }

    @FXML
    void supprimerStaff() {
        Utilisateur sel = tableStaff.getSelectionModel().getSelectedItem();
        if (sel == null) { alerte("Avertissement", "Sélectionnez un employé."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer le compte de « " + sel.getNom() + " » ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK && utilisateurDAO.supprimerUtilisateur(sel.getId())) {
                chargerStaff();
            }
        });
    }

    @FXML
    void seDeconnecter() {
        Session.getInstance().fermer();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/restaurantmanager/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) labelNomGerant.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/restaurantmanager/styles.css").toExternalForm());
            stage.setWidth(900);
            stage.setHeight(520);
            stage.centerOnScreen();
            stage.setScene(scene);
        } catch (Exception e) {
            alerte("Erreur", "Impossible de retourner à la page de connexion.");
        }
    }

    private void alerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
