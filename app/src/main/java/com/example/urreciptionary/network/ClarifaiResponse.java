package com.example.urreciptionary.network;


import com.google.gson.annotations.SerializedName;
import java.util.*;

public class ClarifaiResponse {

    @SerializedName("outputs")
    private List<Output> outputs;

    public List<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Output> outputs) {
        this.outputs = outputs;
    }

    public static class Output {
        @SerializedName("data")
        private Data data;

        @SerializedName("image_url")
        private String imageUrl;

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public static class Data {
            @SerializedName("concepts")
            private List<Concept> concepts;

            public List<Concept> getConcepts() {
                return concepts;
            }

            public void setConcepts(List<Concept> concepts) {
                this.concepts = concepts;
            }

            public static class Concept {
                @SerializedName("name")
                private String name;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }
            }
        }
    }
}