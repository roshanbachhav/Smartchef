package com.example.urreciptionary.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecipeResponse {
    @SerializedName("results")
    private List<Recipe> recipes;

    @SerializedName("offset")
    private int offset;

    @SerializedName("number")
    private int number;

    @SerializedName("totalResults")
    private int totalResults;

    public int getOffset() {
        return offset;
    }

    public int getNumber() {
        return number;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public static class Recipe {

        @SerializedName("title")
        private String title;

        @SerializedName("image")
        private String image;

        @SerializedName("id")
        private int id;

        @SerializedName("readyInMinutes")
        private int readyInMinutes;

        @SerializedName("healthScore")
        private double healthScore;

        @SerializedName("cookTime")
        private int cookTime;

        @SerializedName("aggregateLikes")
        private int likes;

        public String getTitle() {
            return title;
        }

        public String getImage() {
            return image;
        }

        public int getId() {
            return id;
        }

        public int getReadyInMinutes() {
            return readyInMinutes;
        }

        public double getHealthScore() {
            return healthScore;
        }

        public int getCookTime() {
            return cookTime;
        }

        public void setReadyInMinutes(int readyInMinutes) {
            this.readyInMinutes = readyInMinutes;
        }

        public void setHealthScore(double healthScore) {
            this.healthScore = healthScore;
        }

        public void setCookTime(int cookTime) {
            this.cookTime = cookTime;
        }

        public int getLikes() {
            return likes;
        }

        public void setLikes(int likes) {
            this.likes = likes;
        }
    }
}
