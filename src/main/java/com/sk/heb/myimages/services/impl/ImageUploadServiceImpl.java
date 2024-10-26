package com.sk.heb.myimages.services.impl;

import com.sk.heb.myimages.dto.ImageUploadResponse;
import com.sk.heb.myimages.services.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${com.sk.heb.freeimage.key}")
    private String IMAGE_UPLOAD_KEY;

    @Value("${com.sk.heb.freeimage.url}")
    private String IMAGE_UPLOAD_URL;

    @Override
    public CompletableFuture<ResponseEntity<ImageUploadResponse>> uploadImage(@NonNull byte[] fileBytes) {
        return CompletableFuture.supplyAsync(() -> {
            MultiValueMap<String, String> imageFormData = new LinkedMultiValueMap<>();
            imageFormData.add("key", IMAGE_UPLOAD_KEY);
            imageFormData.add("action", "upload");

            // Convert byte array to Base64-encoded string
            String base64EncodedImage = Base64.getEncoder().encodeToString(fileBytes);

            imageFormData.add("source", base64EncodedImage);
            String url = IMAGE_UPLOAD_URL;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(imageFormData, headers);

            return restTemplate.postForEntity(url, request, ImageUploadResponse.class);
        });
    }
}
