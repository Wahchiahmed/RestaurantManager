package com.example.restaurantmanager.Model;

public class TableRestaurant {
    private int numeroTable;
    private String statut; // "LIBRE" ou "OCCUPEE"

    public TableRestaurant(int numeroTable) {
        this.numeroTable = numeroTable;
        this.statut = "LIBRE";
    }

    public int getNumeroTable() { return numeroTable; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}