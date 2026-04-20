package com.example.restaurantmanager.Model;

import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private List<Article> articles;
    private double totalAddition;

    public Commande(int id) {
        this.id = id;
        this.articles = new ArrayList<>();
        this.totalAddition = 0.0;
    }

    public void ajouterArticle(Article article) {
        this.articles.add(article);
        this.totalAddition += article.getPrix();
    }

    public int getId() { return id; }
    public List<Article> getArticles() { return articles; }
    public double getTotalAddition() { return totalAddition; }
}