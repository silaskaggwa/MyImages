package com.sk.heb.myimages.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ImageUploadResponse {
    private int status_code;
    private Success success;
    private Image image;
    private String status_txt;

    @Getter
    @Setter
    public static class Success {
        private String message;
        private int code;
    }

    @Getter
    @Setter
    public static class Image {
        private String name;
        private String original_filename;
        private String url;
        private Thumb thumb;
        private Medium medium;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Thumb {
        private String url;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Medium {
        private String url;
    }
}

