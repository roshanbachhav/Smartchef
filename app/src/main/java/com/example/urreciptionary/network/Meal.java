package com.example.urreciptionary.network;

import com.google.firebase.database.Exclude;

public class Meal {
    private int id;
    private String title;
    private String image;
    private int readyInMinutes;
    private double healthScore;
    private String note;
    private double kcal;
    private double carbs;
    private double fat;
    private double protein;
    private boolean isSelected = false;
    public boolean completed = false;

    @Exclude
    private String firebaseKey;

    public Meal(int id, String title, String image, int readyInMinutes, double healthScore, String note, double kcal, double carbs, double fat, double protein, boolean isSelected, boolean completed) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.readyInMinutes = readyInMinutes;
        this.healthScore = healthScore;
        this.note = note;
        this.kcal = kcal;
        this.carbs = carbs;
        this.fat = fat;
        this.protein = protein;
        this.isSelected = isSelected;
        this.completed = completed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public double getKcal() {
        return kcal;
    }

    public void setKcal(double kcal) {
        this.kcal = kcal;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Meal(){

    }

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

    public double getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(double healthScore) {
        this.healthScore = healthScore;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Exclude
    public String getFirebaseKey() {
        return firebaseKey;
    }

    @Exclude
    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
