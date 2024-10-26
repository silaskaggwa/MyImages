package com.sk.heb.myimages.services;

import com.sk.heb.myimages.dto.ImageTagResponse;
import com.sk.heb.myimages.dto.ImageUploadResponse;
import com.sk.heb.myimages.entity.Image;
import com.sk.heb.myimages.entity.ObjectInImage;
import com.sk.heb.myimages.repository.ImageRepository;
import com.sk.heb.myimages.repository.ObjectsInImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ObjectsInImageRepository objectsInImageRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Image process(String label, byte[] fileBytes) {
        Image newImage = new Image(label);
        CompletableFuture<ResponseEntity<ImageUploadResponse>> uploadFuture = uploadImage(fileBytes);
        CompletableFuture<ResponseEntity<ImageTagResponse>> objectsInImageFuture = uploadFuture.thenCompose(
                uploadResponse -> {

                    ImageUploadResponse imageUploadResponse = uploadResponse.getBody();

                    assert imageUploadResponse != null;
                    String imageUrl = imageUploadResponse.getImage().getUrl();
                    String thumbUrl = imageUploadResponse.getImage().getThumb().getUrl();
                    String mediumUrl = imageUploadResponse.getImage().getMedium().getUrl();
                    System.out.println("imageUrl="+imageUrl);
                    System.out.println("thumbUrl="+thumbUrl);
                    System.out.println("mediumUrl="+mediumUrl);

                    newImage.setImageUrl(mediumUrl);
                    newImage.setThumbnailUrl(thumbUrl);

                    return getObjectsInImage(mediumUrl);
                });
        try {
            ImageTagResponse objectsInImageResp = objectsInImageFuture.get().getBody();
            assert objectsInImageResp != null;
            List<String> objectsInImage = objectsInImageResp.getResult().getTags().stream()
                    .filter(tag -> tag.getConfidence() > 40)
                    .map(tag -> tag.getTag().getEn())
                    .toList();

            return saveImage(newImage, objectsInImage);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<ResponseEntity<ImageUploadResponse>> uploadImage(@NonNull byte[] fileBytes) {
        return CompletableFuture.supplyAsync(() -> {
            MultiValueMap<String, String> imageFormData = new LinkedMultiValueMap<>();
            imageFormData.add("key", "6d207e02198a847aa98d0a2a901485a5");
            imageFormData.add("action", "upload");

            // Convert byte array to Base64-encoded string
            String base64EncodedImage = Base64.getEncoder().encodeToString(fileBytes);

            imageFormData.add("source", base64EncodedImage);
            String url = "https://freeimage.host/api/1/upload";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(imageFormData, headers);

            return restTemplate.postForEntity(url, request, ImageUploadResponse.class);
        });
    }

    public CompletableFuture<ResponseEntity<ImageTagResponse>> getObjectsInImage(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            HttpHeaders headers = new HttpHeaders();
            String authHeader = "Basic YWNjXzhkZDQ0MjBlYTg5ZDI5NDo5ZDZjNmQwMGEwYjQ1MGJmODA5NzhmOWIzOTM5YWEwYw==";
            headers.set("Authorization", authHeader);
            String url = "https://api.imagga.com/v2/tags?image_url=" + imageUrl;
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, ImageTagResponse.class);
        });
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

//    public List<Image> getImagesByTag(String tagName) {
//        return imageRepository.findAllByTagName(tagName);
//    }
//
//    public List<Image> getImagesByTags(List<String> tagNames) {
//        return imageRepository.findAllByTagNames(tagNames, (long) tagNames.size());
//    }
}

