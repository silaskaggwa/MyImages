package com.sk.heb.myimages.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class AppConfig {

    @Value("${com.sk.heb.myimages.allowed-image-file-types}")
    private String allowedImageFileTypes;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Set<String> allowedImageFileTypesSet() {
        return Arrays.stream(allowedImageFileTypes.split(","))
                .collect(Collectors.toSet());
    }
}

