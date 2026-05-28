package com.qiniu.voiceinput.service;

import com.qiniu.voiceinput.dto.AudioFileDTO;
import com.qiniu.voiceinput.dto.PageResponse;
import com.qiniu.voiceinput.entity.AudioFile;
import com.qiniu.voiceinput.exception.ResourceNotFoundException;
import com.qiniu.voiceinput.repository.AudioFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioFileService {

    private final AudioFileRepository audioFileRepository;
    private final QiniuService qiniuService;

    /**
     * 创建音频文件记录
     */
    @Transactional
    public AudioFileDTO createAudioFile(QiniuService.UploadResult uploadResult, Integer duration) {
        AudioFile audioFile = new AudioFile();
        audioFile.setFileName(uploadResult.getFileName());
        audioFile.setQiniuKey(uploadResult.getKey());
        audioFile.setQiniuUrl(uploadResult.getUrl());
        audioFile.setFileSize(uploadResult.getFileSize());
        audioFile.setMimeType(uploadResult.getMimeType());
        audioFile.setDuration(duration != null ? duration : 0);
        audioFile.setStatus(AudioFile.RecognitionStatus.PENDING);

        AudioFile saved = audioFileRepository.save(audioFile);
        log.info("创建音频文件记录: id={}, fileName={}", saved.getId(), saved.getFileName());

        return AudioFileDTO.fromEntity(saved);
    }

    /**
     * 根据ID获取音频文件
     */
    public AudioFileDTO getAudioFile(Long id) {
        AudioFile audioFile = audioFileRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.audioFile(id));
        return AudioFileDTO.fromEntity(audioFile);
    }

    /**
     * 分页查询音频文件
     */
    public PageResponse<AudioFileDTO> listAudioFiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AudioFile> audioFilePage = audioFileRepository.findAllByOrderByCreatedAtDesc(pageable);

        return convertToPageResponse(audioFilePage);
    }

    /**
     * 按状态分页查询
     */
    public PageResponse<AudioFileDTO> listAudioFilesByStatus(AudioFile.RecognitionStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AudioFile> audioFilePage = audioFileRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return convertToPageResponse(audioFilePage);
    }

    /**
     * 删除音频文件
     */
    @Transactional
    public void deleteAudioFile(Long id) {
        AudioFile audioFile = audioFileRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.audioFile(id));

        // 删除七牛云文件
        try {
            qiniuService.deleteFile(audioFile.getQiniuKey());
        } catch (Exception e) {
            log.warn("删除七牛云文件失败，继续删除数据库记录: key={}", audioFile.getQiniuKey(), e);
        }

        // 删除数据库记录
        audioFileRepository.delete(audioFile);
        log.info("删除音频文件: id={}, fileName={}", id, audioFile.getFileName());
    }

    /**
     * 更新识别状态
     */
    @Transactional
    public void updateRecognitionStatus(Long id, AudioFile.RecognitionStatus status, String recognizedText, String errorMessage) {
        AudioFile audioFile = audioFileRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.audioFile(id));

        audioFile.setStatus(status);
        audioFile.setRecognizedText(recognizedText);
        audioFile.setErrorMessage(errorMessage);

        audioFileRepository.save(audioFile);
        log.info("更新识别状态: id={}, status={}", id, status);
    }

    /**
     * 转换为分页响应
     */
    private PageResponse<AudioFileDTO> convertToPageResponse(Page<AudioFile> page) {
        return new PageResponse<>(
                page.getContent().stream().map(AudioFileDTO::fromEntity).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
