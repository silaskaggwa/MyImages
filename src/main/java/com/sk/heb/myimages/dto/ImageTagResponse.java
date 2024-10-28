package com.sk.heb.myimages.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImageTagResponse {
    private Result result;
    private Status status;

    @Getter
    @Setter
    public static class Result {
        private List<TagWrapper> tags;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TagWrapper {
        private double confidence;
        private Tag tag;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Tag {
        private String en;
    }

    @Getter
    @Setter
    public static class Status {
        private String text;
        private String type;
    }
}

