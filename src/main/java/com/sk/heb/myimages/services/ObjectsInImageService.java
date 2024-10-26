package com.sk.heb.myimages.services;

import com.sk.heb.myimages.dto.ImageTagResponse;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

public interface ObjectsInImageService {
    public CompletableFuture<ResponseEntity<ImageTagResponse>> getObjectsInImage(String imageUrl);
}
