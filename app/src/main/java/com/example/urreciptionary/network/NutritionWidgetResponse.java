package com.example.urreciptionary.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NutritionWidgetResponse {

    @SerializedName("calories")
    private String calories;

    @SerializedName("carbs")
    private String carbs;

    @SerializedName("fat")
    private String fat;

    @SerializedName("protein")
    private String protein;

    @SerializedName("nutrients")
    private List<Nutrient> nutrients;

    public List<Nutrient> getNutrients() {
        return nutrients;
    }

    public String getCalories() { return calories; }
    public String getCarbs() { return carbs; }
    public String getFat() { return fat; }
    public String getProtein() { return protein; }

    public static class Nutrient {
        @SerializedName("name")
        private String name;

        @SerializedName("amount")
        private double amount;

        @SerializedName("unit")
        private String unit;

        public String getName() { return name; }
        public double getAmount() { return amount; }
        public String getUnit() { return unit; }
    }
}
