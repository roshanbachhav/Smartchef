package com.example.urreciptionary.network;

import java.util.List;

public class VisionResponse {
    private List<LabelAnnotation> labelAnnotations;

    public List<LabelAnnotation> getLabelAnnotations() {
        return labelAnnotations;
    }

    public static class LabelAnnotation {
        private String description;
        private float score;

        public String getDescription() {
            return description;
        }

        public float getScore() {
            return score;
        }
    }
}
