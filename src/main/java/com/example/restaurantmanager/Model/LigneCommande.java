package com.example.restaurantmanager.Model;

public class LigneCommande {
    private Article article;
    private int quantite;

    public LigneCommande(Article article, int quantite) {
        this.article = article;
        this.quantite = quantite;
    }

    // Getters et Setters
    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getNomArticle() {
        return article.getNom();
    }

    public double getPrixUnitaire() {
        return article.getPrix();
    }

    public double getSousTotal() {
        return article.getPrix() * quantite;
    }

    public void ajouterUn() {
        this.quantite++;
    }

    public void retirerUn() {
        if (this.quantite > 1) {
            this.quantite--;
        }
    }
}