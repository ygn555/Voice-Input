package com.qiniu.voiceinput.controller;

import com.qiniu.voiceinput.dto.ApiResponse;
import com.qiniu.voiceinput.dto.AudioFileDTO;
import com.qiniu.voiceinput.dto.PageResponse;
import com.qiniu.voiceinput.entity.AudioFile;
import com.qiniu.voiceinput.service.AudioFileService;
import com.qiniu.voiceinput.service.QiniuService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final QiniuService qiniuService;
    private final AudioFileService audioFileService;

    /**
     * 上传音频文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AudioFileDTO> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "duration", required = false) Integer duration) {

        log.info("接收音频上传请求: fileName={}, size={}", file.getOriginalFilename(), file.getSize());

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            return ApiResponse.error(400, "只支持音频文件");
        }

        // 验证文件大小（最大50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            return ApiResponse.error(400, "文件大小不能超过50MB");
        }

        // 上传到七牛云
        QiniuService.UploadResult uploadResult = qiniuService.uploadFile(file);

        // 创建数据库记录
        AudioFileDTO audioFileDTO = audioFileService.createAudioFile(uploadResult, duration);

        log.info("音频上传成功: id={}, url={}", audioFileDTO.getId(), audioFileDTO.getQiniuUrl());

        return ApiResponse.success("上传成功", audioFileDTO);
    }

    /**
     * 获取音频文件详情
     */
    @GetMapping("/{id}")
    public ApiResponse<AudioFileDTO> getAudioFile(@PathVariable Long id) {
        AudioFileDTO audioFile = audioFileService.getAudioFile(id);
        return ApiResponse.success(audioFile);
    }

    /**
     * 分页查询音频文件列表
     */
    @GetMapping("/list")
    public ApiResponse<PageResponse<AudioFileDTO>> listAudioFiles(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) AudioFile.RecognitionStatus status) {

        PageResponse<AudioFileDTO> pageResponse;
        if (status != null) {
            pageResponse = audioFileService.listAudioFilesByStatus(status, page, size);
        } else {
            pageResponse = audioFileService.listAudioFiles(page, size);
        }

        return ApiResponse.success(pageResponse);
    }

    /**
     * 删除音频文件
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAudioFile(@PathVariable Long id) {
        audioFileService.deleteAudioFile(id);
        return ApiResponse.success("删除成功", null);
    }
}
