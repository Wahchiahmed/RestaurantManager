package com.example.restaurantmanager.Controller;

import com.example.restaurantmanager.DAO.ArticleDAO;
import com.example.restaurantmanager.DAO.CommandeDAO;
import com.example.restaurantmanager.DAO.TableRestaurantDAO;
import com.example.restaurantmanager.Model.*;
import com.example.restaurantmanager.Utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import javafx.stage.Stage;


import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML private ListView<TableRestaurant> listeTables;
    @FXML private FlowPane conteneurArticles;

    // Changement ici : Le tableau affiche des LigneCommande
    @FXML private TableView<LigneCommande> tableCommande;
    @FXML private TableColumn<LigneCommande, String> colArticle;
    @FXML private TableColumn<LigneCommande, Double> colPrix;
    @FXML private TableColumn<LigneCommande, Integer> colQuantite;
    @FXML private TableColumn<LigneCommande, Double> colTotalLigne;

    @FXML private Label labelTotal;

    private final ArticleDAO articleDAO = new ArticleDAO();
    private final TableRestaurantDAO tableDAO = new TableRestaurantDAO();
    private final CommandeDAO commandeDAO = new CommandeDAO();

    private final Map<Integer, Commande> commandesParTable = new HashMap<>();
    private TableRestaurant tableSelectionnee;

    @FXML
    public void initialize() {
        configurerColonnes();
        chargerTables();
        chargerMenu();

        listeTables.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tableSelectionnee = newVal;
                afficherCommandeTable(newVal.getNumeroTable());
            }
        });
    }

    private void configurerColonnes() {
        // Ces noms doivent correspondre aux noms de propriétés dans LigneCommande.java
        colArticle.setCellValueFactory(new PropertyValueFactory<>("nomArticle"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colTotalLigne.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
    }



    private void chargerTables() {
        ObservableList<TableRestaurant> tables = FXCollections.observableArrayList(tableDAO.getAllTables());
        listeTables.setItems(tables);

        listeTables.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(TableRestaurant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText("Table " + item.getNumeroTable() + " (" + item.getStatut() + ")");
            }
        });
    }

    private void chargerMenu() {
        for (Article article : articleDAO.getAllArticles()) {
            Button btn = new Button(article.getNom() + "\n" + article.getPrix() + " Dt");
            btn.setPrefSize(120, 60);
            btn.setOnAction(e -> ajouterArticleACommande(article));
            conteneurArticles.getChildren().add(btn);
        }
    }

    private void ajouterArticleACommande(Article article) {
        if (tableSelectionnee == null) {
            alerte("Erreur", "Veuillez d'abord sélectionner une table !");
            return;
        }

        int numTable = tableSelectionnee.getNumeroTable();
        Commande cmd = commandesParTable.computeIfAbsent(numTable, k -> new Commande(0));

        // La logique de regroupement par quantité est gérée dans la classe Commande
        cmd.ajouterArticle(article);

        afficherCommandeTable(numTable);
    }

    private void afficherCommandeTable(int numTable) {
        Commande cmd = commandesParTable.get(numTable);
        if (cmd != null) {
            // On affiche les lignes de la commande
            tableCommande.setItems(FXCollections.observableArrayList(cmd.getLignes()));
            labelTotal.setText(String.format("Total : %.2f Dt", cmd.getTotalAddition()));

            tableCommande.refresh();
        } else {
            tableCommande.getItems().clear();
            labelTotal.setText("Total : 0.00 Dt");
        }
    }

    @FXML
    void validerCommandeAction() {
        if (tableSelectionnee == null || !commandesParTable.containsKey(tableSelectionnee.getNumeroTable())) {
            alerte("Erreur", "Aucune commande à valider.");
            return;
        }

        Commande cmd = commandesParTable.get(tableSelectionnee.getNumeroTable());

        if (commandeDAO.sauvegarderCommande(cmd, tableSelectionnee.getNumeroTable())) {
            tableDAO.updateStatut(tableSelectionnee.getNumeroTable(), "OCCUPEE");
            commandesParTable.remove(tableSelectionnee.getNumeroTable());
            chargerTables();
            afficherCommandeTable(tableSelectionnee.getNumeroTable());
            alerte("Succès", "Commande validée !");
        }
    }

    @FXML
    void supprimerArticleAction() {
        if (tableSelectionnee == null) {
            alerte("Erreur", "Veuillez d'abord sélectionner une table.");
            return;
        }

        // Récupération de la LIGNE sélectionnée
        LigneCommande ligneSelectionnee = tableCommande.getSelectionModel().getSelectedItem();

        if (ligneSelectionnee == null) {
            alerte("Avertissement", "Sélectionnez une ligne dans le tableau à supprimer.");
            return;
        }

        Commande cmd = commandesParTable.get(tableSelectionnee.getNumeroTable());
        if (cmd != null) {
            cmd.supprimerLigne(ligneSelectionnee);
            afficherCommandeTable(tableSelectionnee.getNumeroTable());
        }
    }

    @FXML
    void exporterCSVAction() {
        if (tableSelectionnee == null || !commandesParTable.containsKey(tableSelectionnee.getNumeroTable())) {
            alerte("Erreur", "Aucune commande active pour cette table.");
            return;
        }

        Commande cmd = commandesParTable.get(tableSelectionnee.getNumeroTable());
        String nomFichier = "Facture_Table_" + tableSelectionnee.getNumeroTable() + ".csv";

        try (PrintWriter writer = new PrintWriter(new File(nomFichier))) {
            // En-tête colonnes
            writer.println("Article,Prix Unitaire,Quantite,Sous-Total");

            // Lignes articles
            for (LigneCommande ligne : cmd.getLignes()) {
                writer.printf("%s,%.2f,%d,%.2f%n",
                        ligne.getNomArticle(),
                        ligne.getPrixUnitaire(),
                        ligne.getQuantite(),
                        ligne.getSousTotal());
            }

            // Total
            writer.printf("TOTAL,,, %.2f Dt%n", cmd.getTotalAddition());

            alerte("Export Réussi", "Le fichier CSV a été généré : " + nomFichier);
        } catch (FileNotFoundException e) {
            alerte("Erreur", "Impossible de créer le fichier CSV : " + e.getMessage());
        }
    }

    @FXML
    void exporterPDFAction() {
        if (tableSelectionnee == null || !commandesParTable.containsKey(tableSelectionnee.getNumeroTable())) {
            alerte("Erreur", "Aucune commande à exporter.");
            return;
        }

        Commande cmd = commandesParTable.get(tableSelectionnee.getNumeroTable());
        String nomFichier = "Facture_Table_" + tableSelectionnee.getNumeroTable() + ".pdf";

        try {
            // Couleurs personnalisées
            com.itextpdf.kernel.colors.Color couleurPrimaire =
                    new com.itextpdf.kernel.colors.DeviceRgb(30, 80, 140);       // Bleu foncé
            com.itextpdf.kernel.colors.Color couleurAccent =
                    new com.itextpdf.kernel.colors.DeviceRgb(220, 53, 69);       // Rouge vif
            com.itextpdf.kernel.colors.Color couleurEnteteTableau =
                    new com.itextpdf.kernel.colors.DeviceRgb(52, 58, 64);        // Gris foncé
            com.itextpdf.kernel.colors.Color couleurLignePaire =
                    new com.itextpdf.kernel.colors.DeviceRgb(240, 244, 248);     // Bleu très clair
            com.itextpdf.kernel.colors.Color couleurBlancCasse =
                    new com.itextpdf.kernel.colors.DeviceRgb(255, 255, 255);     // Blanc
            com.itextpdf.kernel.colors.Color couleurTexteGris =
                    new com.itextpdf.kernel.colors.DeviceRgb(108, 117, 125);     // Gris texte

            PdfWriter pdfWriter = new PdfWriter(nomFichier);
            PdfDocument pdf = new PdfDocument(pdfWriter);
            Document document = new Document(pdf, com.itextpdf.kernel.geom.PageSize.A4);
            document.setMargins(30, 40, 30, 40);

            // ─────────────── BANDEAU EN-TÊTE ───────────────
            float[] colsHeader = {1f, 1f};
            com.itextpdf.layout.element.Table headerTable =
                    new com.itextpdf.layout.element.Table(colsHeader)
                            .useAllAvailableWidth();

            // Bloc gauche : nom du restaurant
            com.itextpdf.layout.element.Cell cellNomRestaurant =
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph("🍽  RESTAURANT MANAGER")
                                    .setFontSize(20)
                                    .setBold()
                                    .setFontColor(couleurBlancCasse))
                            .add(new Paragraph("Cuisine italienne & méditerranéenne")
                                    .setFontSize(9)
                                    .setItalic()
                                    .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(180, 210, 240)))
                            .setBackgroundColor(couleurPrimaire)
                            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                            .setPadding(15);
            headerTable.addCell(cellNomRestaurant);

            // Bloc droit : infos table et date
            String dateHeure = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm"));
            com.itextpdf.layout.element.Cell cellInfos =
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph("TABLE  N°" + tableSelectionnee.getNumeroTable())
                                    .setFontSize(16)
                                    .setBold()
                                    .setFontColor(couleurBlancCasse)
                                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT))
                            .add(new Paragraph(dateHeure)
                                    .setFontSize(9)
                                    .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(180, 210, 240))
                                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT))
                            .setBackgroundColor(couleurPrimaire)
                            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                            .setPadding(15);
            headerTable.addCell(cellInfos);

            document.add(headerTable);
            document.add(new Paragraph(" ").setFontSize(6)); // espace

            // ─────────────── TITRE REÇU FISCAL ───────────────
            document.add(new Paragraph("REÇU FISCAL")
                    .setFontSize(13)
                    .setBold()
                    .setFontColor(couleurAccent)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(8));

            // ─────────────── TABLEAU DES ARTICLES ───────────────
            float[] colsTableau = {4f, 1.8f, 1.2f, 2f};
            com.itextpdf.layout.element.Table tablePdf =
                    new com.itextpdf.layout.element.Table(colsTableau)
                            .useAllAvailableWidth()
                            .setMarginBottom(12);

            // En-tête du tableau
            String[] entetes = {"Article", "Prix U. (Dt)", "Qté", "Sous-Total (Dt)"};
            for (String entete : entetes) {
                tablePdf.addHeaderCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph(entete).setBold().setFontSize(10).setFontColor(couleurBlancCasse))
                                .setBackgroundColor(couleurEnteteTableau)
                                .setPadding(7)
                                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                );
            }

            // Lignes d'articles (alternance de couleurs)
            int index = 0;
            for (LigneCommande ligne : cmd.getLignes()) {
                com.itextpdf.kernel.colors.Color bgLigne =
                        (index % 2 == 0) ? couleurBlancCasse : couleurLignePaire;

                com.itextpdf.layout.borders.Border bordureCell =
                        new com.itextpdf.layout.borders.SolidBorder(
                                new com.itextpdf.kernel.colors.DeviceRgb(220, 220, 220), 0.5f);

                tablePdf.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(ligne.getNomArticle()).setFontSize(10))
                        .setBackgroundColor(bgLigne).setPadding(6)
                        .setBorderLeft(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setBorderRight(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setBorderTop(bordureCell).setBorderBottom(bordureCell));

                tablePdf.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(String.format("%.2f", ligne.getPrixUnitaire())).setFontSize(10)
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
                        .setBackgroundColor(bgLigne).setPadding(6)
                        .setBorderLeft(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setBorderRight(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setBorderTop(bordureCell).setBorderBottom(bordureCell));

                tablePdf.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(String.valueOf(ligne.getQuantite())).setFontSize(10)
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
                        .setBackgroundColor(bgLigne).setPadding(6)
                        .setBorderLeft(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setBorderRight(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setBorderTop(bordureCell).setBorderBottom(bordureCell));

                tablePdf.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(String.format("%.2f", ligne.getSousTotal())).setFontSize(10).setBold()
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT))
                        .setBackgroundColor(bgLigne).setPadding(6)
                        .setBorderLeft(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setBorderRight(com.itextpdf.layout.borders.Border.NO_BORDER)
                        .setBorderTop(bordureCell).setBorderBottom(bordureCell));

                index++;
            }

            document.add(tablePdf);

            // ─────────────── BLOC TOTAL ───────────────
            float[] colsTotal = {3f, 2f};
            com.itextpdf.layout.element.Table tableTotal =
                    new com.itextpdf.layout.element.Table(colsTotal)
                            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT)
                            .setWidth(220)
                            .setMarginBottom(20);

            tableTotal.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("TOTAL NET À PAYER").setBold().setFontSize(12).setFontColor(couleurBlancCasse))
                    .setBackgroundColor(couleurAccent)
                    .setPadding(10)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));

            tableTotal.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(String.format("%.2f Dt", cmd.getTotalAddition()))
                            .setBold().setFontSize(14).setFontColor(couleurBlancCasse)
                            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT))
                    .setBackgroundColor(couleurAccent)
                    .setPadding(10)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));

            document.add(tableTotal);

            // ─────────────── PIED DE PAGE ───────────────
            document.add(new Paragraph("_____________________________________________")
                    .setFontColor(couleurTexteGris)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setFontSize(10));
            document.add(new Paragraph("Merci de votre visite ! À bientôt 😊")
                    .setFontSize(11).setItalic()
                    .setFontColor(couleurTexteGris)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            document.add(new Paragraph("Conservez ce reçu pour tout litige.")
                    .setFontSize(8)
                    .setFontColor(couleurTexteGris)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.close();
            alerte("Export Réussi", "La facture PDF a été générée : " + nomFichier);

        } catch (Exception e) {
            alerte("Erreur", "Erreur lors de la génération du PDF : " + e.getMessage());
        }
    }
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

        // Mise à jour du statut dans la base de données via le DAO
        tableDAO.updateStatut(tableSelectionnee.getNumeroTable(), "LIBRE");

        // Suppression de la commande en mémoire vive (Map)
        commandesParTable.remove(tableSelectionnee.getNumeroTable());

        // Rafraîchissement de l'interface
        chargerTables();
        afficherCommandeTable(tableSelectionnee.getNumeroTable());

        alerte("Succès", "La table " + tableSelectionnee.getNumeroTable() + " est désormais libre.");
    }

    private void alerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void seDeconnecter() {
        // 1. Vider l'utilisateur en cours de la session mémoire
        Session.getInstance().fermer();

        try {
            // 2. Charger le fichier FXML de la page de connexion
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/restaurantmanager/login-view.fxml"));
            Parent root = loader.load();

            // 3. Récupérer la fenêtre (Stage) actuelle via n'importe quel élément de l'interface (ex: labelTotal)
            Stage stage = (Stage) labelTotal.getScene().getWindow();
            Scene scene = new Scene(root);

            // 4. Appliquer le style CSS
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/restaurantmanager/styles.css").toExternalForm());

            // 5. Redimensionner et centrer la fenêtre pour le login
            stage.setWidth(900);
            stage.setHeight(520);
            stage.centerOnScreen();
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            alerte("Erreur", "Impossible de retourner à la page de connexion : " + e.getMessage());
        }
    }
}