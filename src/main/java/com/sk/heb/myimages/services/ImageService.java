package com.sk.heb.myimages.services;

import com.sk.heb.myimages.dto.ImageTagResponse;
import com.sk.heb.myimages.dto.ImageUploadResponse;
import com.sk.heb.myimages.entity.Image;
import com.sk.heb.myimages.entity.ObjectInImage;
import com.sk.heb.myimages.repository.ImageRepository;
import com.sk.heb.myimages.repository.ObjectsInImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ObjectsInImageRepository objectsInImageRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private ObjectsInImageService objectsInImageService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${com.sk.heb.imagga.min_confidence}")
    private int MIN_CONFIDENCE;

    public Image process(String label, byte[] fileBytes) {
        Image newImage = new Image(label);

        // Step 1: Upload image asynchronously
        CompletableFuture<ResponseEntity<ImageUploadResponse>> uploadFuture = imageUploadService.uploadImage(fileBytes);

        // Step 2: Process image once upload completes and extract tags
        CompletableFuture<Image> processedImageFuture = uploadFuture.thenCompose(uploadResponse -> {
            // Extract image URLs
            ImageUploadResponse uploadResponseBody = getResponseBody(uploadResponse);
            if (uploadResponseBody == null) {
                throw new RuntimeException("Upload failed, response body is null");
            }

            String mediumUrl = uploadResponseBody.getImage().getMedium().getUrl();
            newImage.setImageUrl(mediumUrl);
            newImage.setThumbnailUrl(uploadResponseBody.getImage().getThumb().getUrl());

            // Step 3: Get objects in image based on medium URL
            return objectsInImageService.getObjectsInImage(mediumUrl)
                    .thenApply(objectsInImageResponse -> {
                        List<String> tags = extractValidTags(getResponseBody(objectsInImageResponse));
                        return saveImage(newImage, tags);
                    });
        });

        // Step 4: Return processed image or handle exception
        try {
            return processedImageFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error processing image", e);
        }
    }

    // Helper method to safely extract response body
    private <T> T getResponseBody(ResponseEntity<T> responseEntity) {
        if (responseEntity == null || responseEntity.getBody() == null) {
            return null;
        }
        return responseEntity.getBody();
    }

    // Helper method to extract valid tags from the image response
    private List<String> extractValidTags(ImageTagResponse tagResponse) {
        if (tagResponse == null || tagResponse.getResult() == null) {
            return Collections.emptyList();
        }
        return tagResponse.getResult().getTags().stream()
                .filter(tag -> tag.getConfidence() > MIN_CONFIDENCE)
                .map(tag -> tag.getTag().getEn())
                .toList();
    }

    public Image saveImage(Image image, List<String> objectNames) {

        // Add tags to the image
        for (String objectName : objectNames) {
            ObjectInImage object = objectsInImageRepository.findByName(objectName).orElseGet(() -> {
                ObjectInImage newObject = new ObjectInImage();
                newObject.setName(objectName);
                return objectsInImageRepository.save(newObject);
            });
            image.getObjects().add(object);
        }

        return imageRepository.save(image);
    }

    public List<Image> getAll() {
        return imageRepository.findAll();
    }

    public List<Image> getImagesByObjectNames(Set<String> objects) {
        return imageRepository.findAllByObjectNames(objects);
    }

    public Optional<Image> getImageById(long id) {
        return imageRepository.findById(id);
    }
}

