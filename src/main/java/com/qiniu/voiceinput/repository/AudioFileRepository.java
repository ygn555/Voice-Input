package com.qiniu.voiceinput.repository;

import com.qiniu.voiceinput.entity.AudioFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {

    Page<AudioFile> findByStatusOrderByCreatedAtDesc(AudioFile.RecognitionStatus status, Pageable pageable);

    Page<AudioFile> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<AudioFile> findByStatusAndCreatedAtBefore(AudioFile.RecognitionStatus status, LocalDateTime dateTime);

    long countByStatus(AudioFile.RecognitionStatus status);
}
