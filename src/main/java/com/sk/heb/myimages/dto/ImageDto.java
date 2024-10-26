package com.sk.heb.myimages.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {
    private String id;
    private String label;
    private String url;
    private String thumbnail;
    private Set<String> objects;
}
