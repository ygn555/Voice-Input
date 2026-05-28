package com.qiniu.voiceinput.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audio_files")
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String qiniuKey;

    @Column(nullable = false, length = 1000)
    private String qiniuUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 50)
    private String mimeType;

    @Column(nullable = false)
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecognitionStatus status = RecognitionStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String recognizedText;

    @Column(length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum RecognitionStatus {
        PENDING,      // 待识别
        PROCESSING,   // 识别中
        COMPLETED,    // 已完成
        FAILED        // 失败
    }
}
