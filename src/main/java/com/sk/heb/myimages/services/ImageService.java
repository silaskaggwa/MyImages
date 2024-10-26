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
        CompletableFuture<ResponseEntity<ImageUploadResponse>> uploadFuture = imageUploadService.uploadImage(fileBytes);
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

                    return objectsInImageService.getObjectsInImage(mediumUrl);
                });
        try {
            ImageTagResponse objectsInImageResp = objectsInImageFuture.get().getBody();
            assert objectsInImageResp != null;
            List<String> objectsInImage = objectsInImageResp.getResult().getTags().stream()
                    .filter(tag -> tag.getConfidence() > MIN_CONFIDENCE)
                    .map(tag -> tag.getTag().getEn())
                    .toList();

            return saveImage(newImage, objectsInImage);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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

