package com.qiniu.voiceinput.controller;

import com.qiniu.voiceinput.dto.ApiResponse;
import com.qiniu.voiceinput.service.AsrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/asr")
@RequiredArgsConstructor
public class AsrController {

    private final AsrService asrService;

    /**
     * 提交语音识别任务
     */
    @PostMapping("/recognize/{audioFileId}")
    public ApiResponse<AsrService.AsrResult> recognizeAudio(@PathVariable Long audioFileId) {
        log.info("提交语音识别任务: audioFileId={}", audioFileId);

        AsrService.AsrResult result = asrService.submitAsrTask(audioFileId);

        return ApiResponse.success("识别任务已提交", result);
    }
}
