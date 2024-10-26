package com.sk.heb.myimages.services.impl;

import com.sk.heb.myimages.dto.ImageTagResponse;
import com.sk.heb.myimages.services.ObjectsInImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
public class ObjectsInImageServiceImpl implements ObjectsInImageService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${com.sk.heb.imagga.auth_token}")
    private String IMAGGA_AUTH_TOKEN;

    @Value("${com.sk.heb.imagga.url}")
    private String IMAGGA_URL;

    @Override
    public CompletableFuture<ResponseEntity<ImageTagResponse>> getObjectsInImage(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders headers = new HttpHeaders();
            String authHeader = "Basic " + IMAGGA_AUTH_TOKEN;
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
            String url = IMAGGA_URL + imageUrl;
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, ImageTagResponse.class);
        });
    }
}
