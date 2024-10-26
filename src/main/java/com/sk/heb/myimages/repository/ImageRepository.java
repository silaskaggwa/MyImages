package com.sk.heb.myimages.repository;

import com.sk.heb.myimages.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
//    @Query("SELECT i FROM Image i JOIN i.tags t WHERE t.name = :tagName")
//    List<Image> findAllByTagName(@Param("tagName") String tagName);
//
//    @Query("SELECT i FROM Image i JOIN i.tags t WHERE t.name IN :tagNames GROUP BY i HAVING COUNT(DISTINCT t.name) = :size")
//    List<Image> findAllByObjectNames(@Param("tagNames") List<String> tagNames, @Param("size") Long size);
}

