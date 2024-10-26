package com.sk.heb.myimages.repository;

import com.sk.heb.myimages.entity.ObjectInImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObjectsInImageRepository extends JpaRepository<ObjectInImage, Long> {
    Optional<ObjectInImage> findByName(String name);
}

