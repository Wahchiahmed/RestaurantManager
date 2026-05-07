package com.example.restaurantmanager.Model;

import java.time.LocalDateTime;

public class Utilisateur {

    public enum Role { GERANT, CAISSIER }

    private int id;
    private String nom;
    private String login;
    private String motDePasse;
    private Role role;
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereConnexion;

    public Utilisateur() {}

    public Utilisateur(int id, String nom, String login, Role role, boolean actif) {
        this.id    = id;
        this.nom   = nom;
        this.login = login;
        this.role  = role;
        this.actif = actif;
    }

    // ── Getters ──
    public int getId()                              { return id; }
    public String getNom()                          { return nom; }
    public String getLogin()                        { return login; }
    public String getMotDePasse()                   { return motDePasse; }
    public Role getRole()                           { return role; }
    public boolean isActif()                        { return actif; }
    public LocalDateTime getDateCreation()          { return dateCreation; }
    public LocalDateTime getDerniereConnexion()     { return derniereConnexion; }

    // ── Setters ──
    public void setId(int id)                                           { this.id = id; }
    public void setNom(String nom)                                      { this.nom = nom; }
    public void setLogin(String login)                                  { this.login = login; }
    public void setMotDePasse(String motDePasse)                        { this.motDePasse = motDePasse; }
    public void setRole(Role role)                                      { this.role = role; }
    public void setActif(boolean actif)                                 { this.actif = actif; }
    public void setDateCreation(LocalDateTime dateCreation)             { this.dateCreation = dateCreation; }
    public void setDerniereConnexion(LocalDateTime derniereConnexion)   { this.derniereConnexion = derniereConnexion; }

    public boolean isGerant()  { return Role.GERANT.equals(role); }
    public boolean isCaissier(){ return Role.CAISSIER.equals(role); }

    @Override
    public String toString() { return nom + " (" + role + ")"; }
}