package com.example.restaurantmanager.Model;

public class Entree extends Article {
    public Entree(int id, String nom, double prix, int quantiteStock) {
        super(id, nom, prix, quantiteStock);
    }

    @Override
    public String getType() { return "ENTREE"; }

    @Override
    public String getDescription() { return "Entrée : " + this.nom; }
}