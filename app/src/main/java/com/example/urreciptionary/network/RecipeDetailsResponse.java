package com.example.urreciptionary.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecipeDetailsResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private String image;

    @SerializedName("summary")
    private String summary;

    @SerializedName("vegetarian")
    private boolean vegetarian;

    @SerializedName("extendedIngredients")
    private List<Ingredient> ingredients;

    @SerializedName("instructions")
    private String instructions;

    @SerializedName("diets")
    private List<String> diets;

    @SerializedName("readyInMinutes")
    private int readyInMinutes;

    @SerializedName("cookTime")
    private int cookTime;

    @SerializedName("aggregateLikes")
    private int likes;

    @SerializedName("mealType")
    private String mealType;

    @SerializedName("allergens")
    private List<String> allergens;

    @SerializedName("glutenFree")
    private boolean glutenFree;

    @SerializedName("vegan")
    private boolean vegan;

    @SerializedName("dairyFree")
    private boolean dairyFree;

    @SerializedName("healthScore")
    private double healthScore;

    @SerializedName("cuisines")
    private List<String> cuisines;

    @SerializedName("dishTypes")
    private List<String> dishTypes;

    @SerializedName("occasions")
    private List<String> occasions;


    public boolean isGlutenFree() {
        return glutenFree;
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isDairyFree() {
        return dairyFree;
    }

    @SerializedName("analyzedInstructions")
    private List<AnalyzedInstruction> analyzedInstructions;

    public List<AnalyzedInstruction> getAnalyzedInstructions() {
        return analyzedInstructions;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public List<String> getDiets() {
        return diets;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getLikes() {
        return likes;
    }

    public String getMealType() {
        return mealType;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public static class Ingredient {
        @SerializedName("name")
        private String name;

        @SerializedName("amount")
        private String amount;

        @SerializedName("unit")
        private String unit;

        @SerializedName("measures")
        private Measures measures;

        public String getName() {
            return name;
        }

        public String getAmount() {
            return amount;
        }

        public String getUnit() {
            return unit;
        }

        public Measures getMeasures() {
            return measures;
        }

        public static class Measures {
            @SerializedName("us")
            private MeasureDetails us;

            @SerializedName("metric")
            private MeasureDetails metric;

            public MeasureDetails getUs() {
                return us;
            }

            public MeasureDetails getMetric() {
                return metric;
            }

            public static class MeasureDetails {
                @SerializedName("amount")
                private double amount;

                @SerializedName("unitShort")
                private String unitShort;

                @SerializedName("unitLong")
                private String unitLong;

                public double getAmount() {
                    return amount;
                }

                public String getUnitShort() {
                    return unitShort;
                }

                public String getUnitLong() {
                    return unitLong;
                }
            }
        }
    }

    public double getHealthScore() {
        return healthScore;
    }

    public List<String> getCuisines() {
        return cuisines;
    }

    public List<String> getDishTypes() {
        return dishTypes;
    }

    public List<String> getOccasions() {
        return occasions;
    }

    public static class AnalyzedInstruction {

        @SerializedName("name")
        private String name;

        @SerializedName("steps")
        private List<Step> steps;

        public String getName() {
            return name;
        }

        public List<Step> getSteps() {
            return steps;
        }

        public static class Step {
            @SerializedName("number")
            private int number;

            @SerializedName("step")
            private String step;

            @SerializedName("ingredients")
            private List<Ingredient> ingredients;

            public int getNumber() {
                return number;
            }

            public String getStep() {
                return step;
            }

            public List<Ingredient> getIngredients() {
                return ingredients;
            }
        }
    }
}
