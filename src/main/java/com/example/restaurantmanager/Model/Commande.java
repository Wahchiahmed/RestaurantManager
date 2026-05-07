package com.example.restaurantmanager.Model;

import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private List<LigneCommande> lignes;

    public Commande(int id) {
        this.id = id;
        this.lignes = new ArrayList<>();
    }

    public void ajouterArticle(Article article) {
        // On cherche si l'article existe déjà dans la commande
        for (LigneCommande ligne : lignes) {
            if (ligne.getArticle().getId() == article.getId()) {
                ligne.ajouterUn(); // Si oui, on augmente juste la quantité
                return;
            }
        }
        // Si non, on crée une nouvelle ligne avec une quantité de 1
        lignes.add(new LigneCommande(article, 1));
    }

    public void supprimerLigne(LigneCommande ligne) {
        lignes.remove(ligne);
    }

    public double getTotalAddition() {
        double total = 0;
        for (LigneCommande ligne : lignes) {
            total += ligne.getSousTotal();
        }
        return total;
    }

    public List<LigneCommande> getLignes() {
        return lignes;
    }
}