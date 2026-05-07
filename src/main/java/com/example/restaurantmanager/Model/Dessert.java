package com.example.restaurantmanager.Model;

public class Dessert extends Article {
    public Dessert(int id, String nom, double prix, int quantiteStock) {
        super(id, nom, prix, quantiteStock);
    }

    @Override
    public String getType() { return "DESSERT"; }

    @Override
    public String getDescription() { return "Dessert : " + this.nom; }
}