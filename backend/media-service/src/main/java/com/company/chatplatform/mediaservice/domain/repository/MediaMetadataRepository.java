package com.company.chatplatform.mediaservice.domain.repository;

import com.company.chatplatform.mediaservice.domain.entity.MediaMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaMetadataRepository extends JpaRepository<MediaMetadataEntity, String> {
    List<MediaMetadataEntity> findByUploaderId(String uploaderId);
}
