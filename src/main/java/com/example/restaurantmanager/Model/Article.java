package com.example.restaurantmanager.Model;

public abstract class Article {
    protected int id;
    protected String nom;
    protected double prix;
    protected int quantiteStock;

    public Article(int id, String nom, double prix, int quantiteStock) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public abstract String getDescription();

    public abstract String getType();
}