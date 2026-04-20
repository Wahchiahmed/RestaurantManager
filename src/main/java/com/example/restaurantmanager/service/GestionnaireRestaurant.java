package com.example.restaurantmanager.service;


import com.example.restaurantmanager.Model.Commande;
import java.util.HashMap;
import java.util.Map;

public class GestionnaireRestaurant {

    private Map<Integer, Commande> commandesEnCours;

    public GestionnaireRestaurant() {
        this.commandesEnCours = new HashMap<>();
    }

    public void demarrerCommande(int numeroTable, Commande nouvelleCommande) {
        commandesEnCours.put(numeroTable, nouvelleCommande);
        System.out.println("Commande démarrée pour la table " + numeroTable);
    }

    public Commande getCommandeParTable(int numeroTable) {
        return commandesEnCours.get(numeroTable);
    }
}