package com.example.urreciptionary.network;

import com.google.firebase.database.Exclude;

public class RecipeWithDetails {
    private int id;
    private String title;
    private String image;
    private int readyInMinutes;
    private int healthScore;

    @Exclude
    private String firebaseKey;

    public RecipeWithDetails(int id, String title, String image, int readyInMinutes, int healthScore) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.readyInMinutes = readyInMinutes;
        this.healthScore = healthScore;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public void setReadyInMinutes(int readyInMinutes) {
        this.readyInMinutes = readyInMinutes;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

}

