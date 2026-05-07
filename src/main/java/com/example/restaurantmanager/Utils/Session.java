package com.example.restaurantmanager.Utils;

import com.example.restaurantmanager.Model.Utilisateur;

/**
 * Singleton léger — conserve l'utilisateur connecté pendant toute la session.
 * Appeler Session.getInstance().setUtilisateur(u) après login,
 * et Session.getInstance().getUtilisateur() depuis n'importe quel controller.
 */
public class Session {

    private static Session instance;
    private Utilisateur utilisateur;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public Utilisateur getUtilisateur()                     { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur)     { this.utilisateur = utilisateur; }

    public void fermer() { utilisateur = null; }

    public boolean isConnecte()  { return utilisateur != null; }
    public boolean isGerant()    { return isConnecte() && utilisateur.isGerant(); }
    public boolean isCaissier()  { return isConnecte() && utilisateur.isCaissier(); }
}