package com.example.restaurantmanager.Controller;

import com.example.restaurantmanager.DAO.ArticleDAO;
import com.example.restaurantmanager.DAO.CommandeDAO;
import com.example.restaurantmanager.DAO.TableRestaurantDAO;
import com.example.restaurantmanager.Exception.*;
import com.example.restaurantmanager.Model.*;
import com.example.restaurantmanager.Utils.Session;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    // ── FXML Fields ──────────────────────────────────────────────────────────
    @FXML private ListView<TableRestaurant>          listeTables;
    @FXML private FlowPane                           conteneurArticles;
    @FXML private TableView<LigneCommande>           tableCommande;
    @FXML private TableColumn<LigneCommande, String>  colArticle;
    @FXML private TableColumn<LigneCommande, Double>  colPrix;
    @FXML private TableColumn<LigneCommande, Integer> colQuantite;
    @FXML private TableColumn<LigneCommande, Double>  colTotalLigne;
    @FXML private Label labelTotal;

    // ── DAOs ─────────────────────────────────────────────────────────────────
    private final ArticleDAO         articleDAO  = new ArticleDAO();
    private final TableRestaurantDAO tableDAO    = new TableRestaurantDAO();
    private final CommandeDAO        commandeDAO = new CommandeDAO();

    // ── État interne ─────────────────────────────────────────────────────────
    private final Map<Integer, Commande> commandesParTable = new HashMap<>();
    private TableRestaurant tableSelectionnee;

    // =========================================================================
    // INITIALISATION
    // =========================================================================

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerTables();
        chargerMenu();

        listeTables.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        tableSelectionnee = newVal;
                        afficherCommandeTable(newVal.getNumeroTable());
                    }
                });
    }

    private void configurerColonnes() {
        colArticle.setCellValueFactory(new PropertyValueFactory<>("nomArticle"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colTotalLigne.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
    }

    // =========================================================================
    // CHARGEMENT DES DONNÉES
    // =========================================================================

    private void chargerTables() {
        try {
            ObservableList<TableRestaurant> tables =
                    FXCollections.observableArrayList(tableDAO.getAllTables());
            listeTables.setItems(tables);

            listeTables.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(TableRestaurant item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText("Table " + item.getNumeroTable()
                            + " (" + item.getStatut() + ")");
                }
            });

        } catch (ConnexionException e) {
            alerte("Erreur de connexion",
                    "Impossible de charger les tables.\n" + e.getMessage());

        } catch (TableDAOException e) {
            alerte("Erreur BDD",
                    "Erreur lors du chargement des tables.\n" + e.getMessage());
        }
    }

    private void chargerMenu() {
        try {
            List<Article> articles = articleDAO.getAllArticles();
            conteneurArticles.getChildren().clear();

            for (Article article : articles) {
                Button btn = new Button(
                        article.getNom() + "\n" + article.getPrix() + " Dt");
                btn.setPrefSize(120, 60);
                btn.setOnAction(e -> ajouterArticleACommande(article));
                conteneurArticles.getChildren().add(btn);
            }

        } catch (ConnexionException e) {
            alerte("Erreur de connexion",
                    "Impossible de charger le menu.\n" + e.getMessage());

        } catch (ArticleInvalideException e) {
            alerte("Donnée invalide",
                    "Un article en base a un type inconnu.\n" + e.getMessage());

        } catch (ArticleDAOException e) {
            alerte("Erreur BDD",
                    "Erreur lors du chargement du menu.\n" + e.getMessage());
        }
    }

    // =========================================================================
    // GESTION DES COMMANDES
    // =========================================================================

    private void ajouterArticleACommande(Article article) {
        if (tableSelectionnee == null) {
            alerte("Erreur", "Veuillez d'abord sélectionner une table !");
            return;
        }
        int numTable = tableSelectionnee.getNumeroTable();
        Commande cmd = commandesParTable.computeIfAbsent(
                numTable, k -> new Commande(0));
        cmd.ajouterArticle(article);
        afficherCommandeTable(numTable);
    }

    private void afficherCommandeTable(int numTable) {
        Commande cmd = commandesParTable.get(numTable);
        if (cmd != null) {
            tableCommande.setItems(
                    FXCollections.observableArrayList(cmd.getLignes()));
            labelTotal.setText(
                    String.format("Total : %.2f Dt", cmd.getTotalAddition()));
            tableCommande.refresh();
        } else {
            tableCommande.getItems().clear();
            labelTotal.setText("Total : 0.00 Dt");
        }
    }

    @FXML
    void validerCommandeAction() {
        if (tableSelectionnee == null
                || !commandesParTable.containsKey(
                tableSelectionnee.getNumeroTable())) {
            alerte("Erreur", "Aucune commande à valider.");
            return;
        }

        Commande cmd = commandesParTable.get(tableSelectionnee.getNumeroTable());

        try {
            // 1. Persister la commande
            commandeDAO.sauvegarderCommande(
                    cmd, tableSelectionnee.getNumeroTable());

            // 2. Marquer la table comme occupée
            tableDAO.updateStatut(
                    tableSelectionnee.getNumeroTable(), "OCCUPEE");

            // 3. Mettre à jour l'interface
            commandesParTable.remove(tableSelectionnee.getNumeroTable());
            chargerTables();
            afficherCommandeTable(tableSelectionnee.getNumeroTable());
            alerte("Succès", "Commande validée !");

        } catch (CommandeVideException e) {
            alerte("Commande vide", e.getMessage());

        } catch (TableOccupeeException e) {
            alerte("Table déjà occupée", e.getMessage());

        } catch (ConnexionException e) {
            alerte("Erreur de connexion",
                    "Impossible de joindre la base de données.\n"
                            + e.getMessage());

        } catch (CommandeDAOException e) {
            alerte("Erreur BDD",
                    "La commande n'a pas pu être enregistrée.\n"
                            + e.getMessage());

        } catch (TableDAOException e) {
            // Commande enregistrée mais statut table non mis à jour
            alerte("Avertissement",
                    "Commande enregistrée, mais le statut de la table "
                            + "n'a pas pu être mis à jour.\n" + e.getMessage());
        }
    }

    @FXML
    void supprimerArticleAction() {
        if (tableSelectionnee == null) {
            alerte("Erreur", "Veuillez d'abord sélectionner une table.");
            return;
        }
        LigneCommande ligneSelectionnee =
                tableCommande.getSelectionModel().getSelectedItem();
        if (ligneSelectionnee == null) {
            alerte("Avertissement",
                    "Sélectionnez une ligne dans le tableau à supprimer.");
            return;
        }
        Commande cmd = commandesParTable.get(
                tableSelectionnee.getNumeroTable());
        if (cmd != null) {
            cmd.supprimerLigne(ligneSelectionnee);
            afficherCommandeTable(tableSelectionnee.getNumeroTable());
        }
    }

    // =========================================================================
    // LIBÉRATION DE TABLE
    // =========================================================================

    @FXML
    void libererTableAction() {
        if (tableSelectionnee == null) {
            alerte("Erreur", "Veuillez d'abord sélectionner une table.");
            return;
        }
        if ("LIBRE".equals(tableSelectionnee.getStatut())) {
            alerte("Information", "Cette table est déjà libre !");
            return;
        }

        try {
            tableDAO.updateStatut(
                    tableSelectionnee.getNumeroTable(), "LIBRE");
            commandesParTable.remove(tableSelectionnee.getNumeroTable());
            chargerTables();
            afficherCommandeTable(tableSelectionnee.getNumeroTable());
            alerte("Succès", "La table "
                    + tableSelectionnee.getNumeroTable()
                    + " est désormais libre.");

        } catch (TableOccupeeException e) {
            // Ne peut pas arriver (on passe à LIBRE), mais géré par sécurité
            alerte("Erreur inattendue", e.getMessage());

        } catch (ConnexionException e) {
            alerte("Erreur de connexion",
                    "Impossible de libérer la table.\n" + e.getMessage());

        } catch (TableDAOException e) {
            alerte("Erreur BDD",
                    "Erreur lors de la libération de la table.\n"
                            + e.getMessage());
        }
    }

    // =========================================================================
    // EXPORT CSV
    // =========================================================================

    @FXML
    void exporterCSVAction() {
        if (tableSelectionnee == null
                || !commandesParTable.containsKey(
                tableSelectionnee.getNumeroTable())) {
            alerte("Erreur", "Aucune commande active pour cette table.");
            return;
        }

        Commande cmd = commandesParTable.get(
                tableSelectionnee.getNumeroTable());
        String nomFichier =
                "Facture_Table_" + tableSelectionnee.getNumeroTable() + ".csv";

        try (PrintWriter writer = new PrintWriter(new File(nomFichier))) {
            writer.println("Article,Prix Unitaire,Quantite,Sous-Total");

            for (LigneCommande ligne : cmd.getLignes()) {
                writer.printf("%s,%.2f,%d,%.2f%n",
                        ligne.getNomArticle(),
                        ligne.getPrixUnitaire(),
                        ligne.getQuantite(),
                        ligne.getSousTotal());
            }
            writer.printf("TOTAL,,, %.2f Dt%n", cmd.getTotalAddition());
            alerte("Export Réussi",
                    "Le fichier CSV a été généré : " + nomFichier);

        } catch (FileNotFoundException e) {
            alerte("Erreur",
                    "Impossible de créer le fichier CSV : " + e.getMessage());
        }
    }

    // =========================================================================
    // EXPORT PDF
    // =========================================================================

    @FXML
    void exporterPDFAction() {
        if (tableSelectionnee == null
                || !commandesParTable.containsKey(
                tableSelectionnee.getNumeroTable())) {
            alerte("Erreur", "Aucune commande à exporter.");
            return;
        }

        Commande cmd = commandesParTable.get(
                tableSelectionnee.getNumeroTable());
        String nomFichier =
                "Facture_Table_" + tableSelectionnee.getNumeroTable() + ".pdf";

        try {
            // ── Couleurs ─────────────────────────────────────────────────────
            com.itextpdf.kernel.colors.Color couleurPrimaire =
                    new com.itextpdf.kernel.colors.DeviceRgb(30, 80, 140);
            com.itextpdf.kernel.colors.Color couleurAccent =
                    new com.itextpdf.kernel.colors.DeviceRgb(220, 53, 69);
            com.itextpdf.kernel.colors.Color couleurEnteteTableau =
                    new com.itextpdf.kernel.colors.DeviceRgb(52, 58, 64);
            com.itextpdf.kernel.colors.Color couleurLignePaire =
                    new com.itextpdf.kernel.colors.DeviceRgb(240, 244, 248);
            com.itextpdf.kernel.colors.Color couleurBlanc =
                    new com.itextpdf.kernel.colors.DeviceRgb(255, 255, 255);
            com.itextpdf.kernel.colors.Color couleurTexteGris =
                    new com.itextpdf.kernel.colors.DeviceRgb(108, 117, 125);

            PdfWriter   pdfWriter = new PdfWriter(nomFichier);
            PdfDocument pdf       = new PdfDocument(pdfWriter);
            Document    document  = new Document(
                    pdf, com.itextpdf.kernel.geom.PageSize.A4);
            document.setMargins(30, 40, 30, 40);

            // ── En-tête ───────────────────────────────────────────────────
            float[] colsHeader = {1f, 1f};
            com.itextpdf.layout.element.Table headerTable =
                    new com.itextpdf.layout.element.Table(colsHeader)
                            .useAllAvailableWidth();

            headerTable.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph("🍽  RESTAURANT MANAGER")
                                    .setFontSize(20).setBold()
                                    .setFontColor(couleurBlanc))
                            .add(new Paragraph("Cuisine italienne & méditerranéenne")
                                    .setFontSize(9).setItalic()
                                    .setFontColor(new com.itextpdf.kernel.colors
                                            .DeviceRgb(180, 210, 240)))
                            .setBackgroundColor(couleurPrimaire)
                            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                            .setPadding(15));

            String dateHeure = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy  HH:mm"));

            headerTable.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(
                                    "TABLE  N°" + tableSelectionnee.getNumeroTable())
                                    .setFontSize(16).setBold()
                                    .setFontColor(couleurBlanc)
                                    .setTextAlignment(com.itextpdf.layout.properties
                                            .TextAlignment.RIGHT))
                            .add(new Paragraph(dateHeure)
                                    .setFontSize(9)
                                    .setFontColor(new com.itextpdf.kernel.colors
                                            .DeviceRgb(180, 210, 240))
                                    .setTextAlignment(com.itextpdf.layout.properties
                                            .TextAlignment.RIGHT))
                            .setBackgroundColor(couleurPrimaire)
                            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                            .setPadding(15));

            document.add(headerTable);
            document.add(new Paragraph(" ").setFontSize(6));

            // ── Titre ─────────────────────────────────────────────────────
            document.add(new Paragraph("REÇU FISCAL")
                    .setFontSize(13).setBold()
                    .setFontColor(couleurAccent)
                    .setTextAlignment(com.itextpdf.layout.properties
                            .TextAlignment.CENTER)
                    .setMarginBottom(8));

            // ── Tableau articles ──────────────────────────────────────────
            float[] colsTableau = {4f, 1.8f, 1.2f, 2f};
            com.itextpdf.layout.element.Table tablePdf =
                    new com.itextpdf.layout.element.Table(colsTableau)
                            .useAllAvailableWidth()
                            .setMarginBottom(12);

            String[] entetes =
                    {"Article", "Prix U. (Dt)", "Qté", "Sous-Total (Dt)"};
            for (String entete : entetes) {
                tablePdf.addHeaderCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph(entete)
                                        .setBold().setFontSize(10)
                                        .setFontColor(couleurBlanc))
                                .setBackgroundColor(couleurEnteteTableau)
                                .setPadding(7)
                                .setBorder(com.itextpdf.layout.borders
                                        .Border.NO_BORDER));
            }

            int index = 0;
            for (LigneCommande ligne : cmd.getLignes()) {
                com.itextpdf.kernel.colors.Color bgLigne =
                        (index % 2 == 0) ? couleurBlanc : couleurLignePaire;
                com.itextpdf.layout.borders.Border bordure =
                        new com.itextpdf.layout.borders.SolidBorder(
                                new com.itextpdf.kernel.colors
                                        .DeviceRgb(220, 220, 220), 0.5f);

                tablePdf.addCell(cellLigne(
                        ligne.getNomArticle(), bgLigne, bordure,
                        com.itextpdf.layout.properties.TextAlignment.LEFT));
                tablePdf.addCell(cellLigne(
                        String.format("%.2f", ligne.getPrixUnitaire()),
                        bgLigne, bordure,
                        com.itextpdf.layout.properties.TextAlignment.CENTER));
                tablePdf.addCell(cellLigne(
                        String.valueOf(ligne.getQuantite()),
                        bgLigne, bordure,
                        com.itextpdf.layout.properties.TextAlignment.CENTER));
                tablePdf.addCell(cellLigne(
                        String.format("%.2f", ligne.getSousTotal()),
                        bgLigne, bordure,
                        com.itextpdf.layout.properties.TextAlignment.RIGHT));
                index++;
            }
            document.add(tablePdf);

            // ── Bloc total ────────────────────────────────────────────────
            float[] colsTotal = {3f, 2f};
            com.itextpdf.layout.element.Table tableTotal =
                    new com.itextpdf.layout.element.Table(colsTotal)
                            .setHorizontalAlignment(com.itextpdf.layout.properties
                                    .HorizontalAlignment.RIGHT)
                            .setWidth(220)
                            .setMarginBottom(20);

            tableTotal.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph("TOTAL NET À PAYER")
                                    .setBold().setFontSize(12)
                                    .setFontColor(couleurBlanc))
                            .setBackgroundColor(couleurAccent)
                            .setPadding(10)
                            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            tableTotal.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(String.format(
                                    "%.2f Dt", cmd.getTotalAddition()))
                                    .setBold().setFontSize(14)
                                    .setFontColor(couleurBlanc)
                                    .setTextAlignment(com.itextpdf.layout.properties
                                            .TextAlignment.RIGHT))
                            .setBackgroundColor(couleurAccent)
                            .setPadding(10)
                            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            document.add(tableTotal);

            // ── Pied de page ──────────────────────────────────────────────
            document.add(new Paragraph(
                    "_____________________________________________")
                    .setFontColor(couleurTexteGris)
                    .setTextAlignment(com.itextpdf.layout.properties
                            .TextAlignment.CENTER)
                    .setFontSize(10));
            document.add(new Paragraph("Merci de votre visite ! À bientôt 😊")
                    .setFontSize(11).setItalic()
                    .setFontColor(couleurTexteGris)
                    .setTextAlignment(com.itextpdf.layout.properties
                            .TextAlignment.CENTER));
            document.add(new Paragraph("Conservez ce reçu pour tout litige.")
                    .setFontSize(8)
                    .setFontColor(couleurTexteGris)
                    .setTextAlignment(com.itextpdf.layout.properties
                            .TextAlignment.CENTER));

            document.close();
            alerte("Export Réussi",
                    "La facture PDF a été générée : " + nomFichier);

        } catch (Exception e) {
            alerte("Erreur",
                    "Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }

    /**
     * Crée une cellule de tableau PDF avec style uniforme.
     */
    private com.itextpdf.layout.element.Cell cellLigne(
            String texte,
            com.itextpdf.kernel.colors.Color bg,
            com.itextpdf.layout.borders.Border bordure,
            com.itextpdf.layout.properties.TextAlignment alignement) {

        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(texte).setFontSize(10)
                        .setTextAlignment(alignement))
                .setBackgroundColor(bg)
                .setPadding(6)
                .setBorderLeft(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBorderRight(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBorderTop(bordure)
                .setBorderBottom(bordure);
    }

    // =========================================================================
    // DÉCONNEXION & UTILITAIRES
    // =========================================================================

    @FXML
    void seDeconnecter() {
        Session.getInstance().fermer();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/restaurantmanager/login-view.fxml"));
            Parent root  = loader.load();
            Stage  stage = (Stage) labelTotal.getScene().getWindow();
            Scene  scene = new Scene(root);
            scene.getStylesheets().add(getClass()
                    .getResource("/com/example/restaurantmanager/styles.css")
                    .toExternalForm());
            stage.setWidth(900);
            stage.setHeight(520);
            stage.centerOnScreen();
            stage.setScene(scene);

        } catch (Exception e) {
            alerte("Erreur",
                    "Impossible de retourner à la page de connexion : "
                            + e.getMessage());
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