package com.sk.heb.myimages.services;

import com.sk.heb.myimages.dto.ImageUploadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.concurrent.CompletableFuture;

public interface ImageUploadService {
    public CompletableFuture<ResponseEntity<ImageUploadResponse>> uploadImage(@NonNull byte[] fileBytes);
}
