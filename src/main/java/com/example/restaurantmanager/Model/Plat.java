package com.example.restaurantmanager.Model;

public class Plat extends Article {

    public Plat(int id, String nom, double prix, int quantiteStock) {
        super(id, nom, prix, quantiteStock);
    }

    @Override
    public String getDescription() {
        return "Plat : " + this.nom + " (" + this.prix + " Dt)";
    }
}
