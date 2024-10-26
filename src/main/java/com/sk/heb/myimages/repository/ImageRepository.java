package com.sk.heb.myimages.repository;

import com.sk.heb.myimages.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("SELECT i FROM Image i JOIN i.objects o WHERE o.name IN :objects GROUP BY i")
    List<Image> findAllByObjectNames(@Param("objects") Set<String> objects);
}

