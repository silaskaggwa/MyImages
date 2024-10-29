package com.sk.heb.myimages.services;

import com.sk.heb.myimages.dto.ImageTagResponse;
import com.sk.heb.myimages.dto.ImageUploadResponse;
import com.sk.heb.myimages.entity.Image;
import com.sk.heb.myimages.entity.ObjectInImage;
import com.sk.heb.myimages.repository.ImageRepository;
import com.sk.heb.myimages.repository.ObjectsInImageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
public class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ObjectsInImageRepository objectsInImageRepository;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private ObjectsInImageService objectsInImageService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ImageService imageService;

    @Value("${com.sk.heb.imagga.min_confidence}")
    private int MIN_CONFIDENCE;

    @Test
    public void testProcess_withObjectDetection() throws ExecutionException, InterruptedException {
        // Arrange
        String label = "Test Image";
        boolean detectObjects = true;
        byte[] fileBytes = new byte[] {1, 2, 3};

        Image newImage = new Image(label);
        ImageUploadResponse uploadResponse = new ImageUploadResponse();
        ImageUploadResponse.Image imageInResponse = new ImageUploadResponse.Image();
        imageInResponse.setMedium(new ImageUploadResponse.Medium("url"));
        imageInResponse.setThumb(new ImageUploadResponse.Thumb());
        uploadResponse.setImage(imageInResponse);

        ResponseEntity<ImageUploadResponse> uploadResponseEntity = new ResponseEntity<>(uploadResponse, HttpStatus.OK);

        CompletableFuture<ResponseEntity<ImageUploadResponse>> uploadFuture = CompletableFuture.completedFuture(uploadResponseEntity);

        when(imageUploadService.uploadImage(fileBytes)).thenReturn(uploadFuture);
        when(imageRepository.save(any(Image.class))).thenReturn(newImage);

        // Mock object detection response
        ImageTagResponse imageTagsResponse = new ImageTagResponse();
        ImageTagResponse.Result result = new ImageTagResponse.Result();
        result.setTags(List.of(new ImageTagResponse.TagWrapper(61, new ImageTagResponse.Tag("Object1"))));
        imageTagsResponse.setResult(result);
        CompletableFuture<ResponseEntity<ImageTagResponse>> objectResponseFuture = CompletableFuture.completedFuture(ResponseEntity.ok(imageTagsResponse));
        when(objectsInImageService.getObjectsInImage(anyString())).thenReturn(objectResponseFuture);

        // Act
        Image resultImage = imageService.process(label, detectObjects, fileBytes);

        // Assert
        assertNotNull(resultImage);
        assertEquals(label, resultImage.getLabel());
    }

    @Test
    public void testProcess_withoutObjectDetection() {
        // Arrange
        String label = "Test Image No Objects";
        boolean detectObjects = false;
        byte[] fileBytes = new byte[] {1, 2, 3};

        Image newImage = new Image(label);
        ImageUploadResponse uploadResponse = new ImageUploadResponse();
        ImageUploadResponse.Image imageInResponse = new ImageUploadResponse.Image();
        imageInResponse.setMedium(new ImageUploadResponse.Medium());
        imageInResponse.setThumb(new ImageUploadResponse.Thumb());
        uploadResponse.setImage(imageInResponse);
        ResponseEntity<ImageUploadResponse> uploadResponseEntity = new ResponseEntity<>(uploadResponse, HttpStatus.OK);

        CompletableFuture<ResponseEntity<ImageUploadResponse>> uploadFuture = CompletableFuture.completedFuture(uploadResponseEntity);

        when(imageUploadService.uploadImage(fileBytes)).thenReturn(uploadFuture);
        when(imageRepository.save(any(Image.class))).thenReturn(newImage);

        // Act
        Image resultImage = imageService.process(label, detectObjects, fileBytes);

        // Assert
        assertNotNull(resultImage);
        assertEquals(label, resultImage.getLabel());
    }

    @Test
    public void testSaveImage_withNewObjects() {
        // Arrange
        Image image = new Image("Test Image");
        List<String> objectNames = List.of("Object1", "Object2");

        ObjectInImage object1 = new ObjectInImage();
        object1.setName("Object1");

        ObjectInImage object2 = new ObjectInImage();
        object2.setName("Object2");

        when(objectsInImageRepository.findByName("Object1")).thenReturn(Optional.empty());
        when(objectsInImageRepository.findByName("Object2")).thenReturn(Optional.empty());
        when(objectsInImageRepository.save(any(ObjectInImage.class)))
                .thenReturn(object1)
                .thenReturn(object2);
        when(imageRepository.save(any(Image.class))).thenReturn(image);

        // Act
        Image resultImage = imageService.saveImage(image, objectNames);

        // Assert
        assertEquals(2, resultImage.getObjects().size());
        verify(objectsInImageRepository, times(2)).save(any(ObjectInImage.class));
        verify(imageRepository).save(image);
    }

    @Test
    public void testGetAll() {
        // Arrange
        List<Image> imageList = List.of(new Image("Image1"), new Image("Image2"));
        when(imageRepository.findAll()).thenReturn(imageList);

        // Act
        List<Image> result = imageService.getAll();

        // Assert
        assertEquals(2, result.size());
        verify(imageRepository).findAll();
    }

    @Test
    public void testGetImagesByObjectNames() {
        // Arrange
        Set<String> objects = Set.of("Object1", "Object2");
        List<Image> imageList = List.of(new Image("Image1"));
        when(imageRepository.findAllByObjectNames(objects)).thenReturn(imageList);

        // Act
        List<Image> result = imageService.getImagesByObjectNames(objects);

        // Assert
        assertEquals(1, result.size());
        verify(imageRepository).findAllByObjectNames(objects);
    }

    @Test
    public void testGetImageById() {
        // Arrange
        Image image = new Image("Test Image");
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        // Act
        Optional<Image> result = imageService.getImageById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Image", result.get().getLabel());
        verify(imageRepository).findById(1L);
    }
}
