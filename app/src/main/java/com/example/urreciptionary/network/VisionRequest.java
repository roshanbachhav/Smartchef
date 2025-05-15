package com.example.urreciptionary.network;

import java.util.*;

public class VisionRequest {

    private List<AnnotateImageRequest> requests;

    public VisionRequest(List<AnnotateImageRequest> requests) {
        this.requests = requests;
    }

    public List<AnnotateImageRequest> getRequests() {
        return requests;
    }

    public static class AnnotateImageRequest {
        private Image image;
        private List<Feature> features;

        public AnnotateImageRequest(Image image, List<Feature> features) {
            this.image = image;
            this.features = features;
        }
    }

    public static class Image {
        private String content;

        public Image(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    public static class Feature {
        private String type;

        public Feature(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

}