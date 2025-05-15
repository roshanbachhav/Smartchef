package com.example.urreciptionary.network;

import com.google.gson.annotations.SerializedName;

public class IngredientSuggestion {
    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
