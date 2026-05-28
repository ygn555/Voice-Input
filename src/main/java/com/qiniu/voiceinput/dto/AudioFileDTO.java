package com.qiniu.voiceinput.dto;

import com.qiniu.voiceinput.entity.AudioFile;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AudioFileDTO {

    private Long id;
    private String fileName;
    private String qiniuUrl;
    private Long fileSize;
    private String mimeType;
    private Integer duration;
    private AudioFile.RecognitionStatus status;
    private String recognizedText;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AudioFileDTO fromEntity(AudioFile entity) {
        AudioFileDTO dto = new AudioFileDTO();
        dto.setId(entity.getId());
        dto.setFileName(entity.getFileName());
        dto.setQiniuUrl(entity.getQiniuUrl());
        dto.setFileSize(entity.getFileSize());
        dto.setMimeType(entity.getMimeType());
        dto.setDuration(entity.getDuration());
        dto.setStatus(entity.getStatus());
        dto.setRecognizedText(entity.getRecognizedText());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
