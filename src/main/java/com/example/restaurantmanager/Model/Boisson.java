package com.example.restaurantmanager.Model;


public class Boisson extends Article {

    public Boisson(int id, String nom, double prix, int quantiteStock) {
        super(id, nom, prix, quantiteStock);
    }

    @Override
    public String getDescription() {
        return "Boisson : " + this.nom + " (" + this.prix + " Dt)";
    }
    @Override
    public String getType() {
        return "BOISSON";
    }
}
