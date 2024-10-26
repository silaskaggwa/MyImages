package com.sk.heb.myimages.controllers;

import com.sk.heb.myimages.entity.Image;
import com.sk.heb.myimages.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

//    @GetMapping("/images")
//    public List<Image> getAllImages(){
//
//    }

    @PostMapping
    public ResponseEntity<Image> saveImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart(name = "label", required = false) String label) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        String fileName = file.getOriginalFilename();
        String imageLabel = Optional.ofNullable(label).orElse(fileName);

        Image newImage = null;
        try {
            byte[] fileContent = file.getBytes();
            newImage = imageService.process(imageLabel, fileContent);
        } catch (IOException e) {
            System.out.println("File upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok(newImage);
    }

}
