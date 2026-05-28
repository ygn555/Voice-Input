package com.qiniu.voiceinput.service;

import com.google.gson.Gson;
import com.qiniu.util.Auth;
import com.qiniu.voiceinput.config.QiniuConfig;
import com.qiniu.voiceinput.entity.AudioFile;
import com.qiniu.voiceinput.exception.BusinessException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsrService {

    private final Auth auth;
    private final QiniuConfig qiniuConfig;
    private final AudioFileService audioFileService;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * 提交语音识别任务
     */
    public AsrResult submitAsrTask(Long audioFileId) {
        com.qiniu.voiceinput.dto.AudioFileDTO audioFile = audioFileService.getAudioFile(audioFileId);

        // 更新状态为处理中
        audioFileService.updateRecognitionStatus(
                audioFileId,
                AudioFile.RecognitionStatus.PROCESSING,
                null,
                null
        );

        try {
            // 构建请求体
            AsrRequest asrRequest = new AsrRequest();
            asrRequest.setData(new AsrRequest.Data(audioFile.getQiniuUrl()));

            String requestBody = new Gson().toJson(asrRequest);

            // 生成认证token
            String token = generateAsrToken(requestBody);

            // 发送请求
            Request request = new Request.Builder()
                    .url(qiniuConfig.getAsr().getUrl())
                    .addHeader("Authorization", "Qiniu " + token)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    log.error("ASR请求失败: code={}, body={}", response.code(), responseBody);
                    audioFileService.updateRecognitionStatus(
                            audioFileId,
                            AudioFile.RecognitionStatus.FAILED,
                            null,
                            "ASR请求失败: " + responseBody
                    );
                    throw new BusinessException("语音识别请求失败");
                }

                AsrResponse asrResponse = new Gson().fromJson(responseBody, AsrResponse.class);

                // 提取识别结果
                String recognizedText = extractRecognizedText(asrResponse);

                // 更新识别结果
                audioFileService.updateRecognitionStatus(
                        audioFileId,
                        AudioFile.RecognitionStatus.COMPLETED,
                        recognizedText,
                        null
                );

                log.info("ASR识别成功: audioFileId={}, text={}", audioFileId, recognizedText);

                return new AsrResult(audioFileId, recognizedText, "SUCCESS");
            }

        } catch (IOException e) {
            log.error("ASR请求异常: audioFileId={}", audioFileId, e);
            audioFileService.updateRecognitionStatus(
                    audioFileId,
                    AudioFile.RecognitionStatus.FAILED,
                    null,
                    "网络异常: " + e.getMessage()
            );
            throw new BusinessException("语音识别失败");
        }
    }

    /**
     * 生成ASR认证token
     */
    private String generateAsrToken(String requestBody) {
        String path = "/asr/v1";
        return auth.signQiniuAuthorization(path, "POST", requestBody.getBytes(), "application/json");
    }

    /**
     * 提取识别文本
     */
    private String extractRecognizedText(AsrResponse response) {
        if (response == null || response.getResult() == null || response.getResult().isEmpty()) {
            return "";
        }

        StringBuilder text = new StringBuilder();
        for (AsrResponse.ResultItem item : response.getResult()) {
            if (item.getText() != null) {
                text.append(item.getText());
            }
        }

        return text.toString().trim();
    }

    @Data
    public static class AsrRequest {
        private Data data;

        @lombok.Data
        public static class Data {
            private String uri;

            public Data(String uri) {
                this.uri = uri;
            }
        }
    }

    @Data
    public static class AsrResponse {
        private java.util.List<ResultItem> result;

        @Data
        public static class ResultItem {
            private String text;
            private Double confidence;
        }
    }

    @Data
    @lombok.AllArgsConstructor
    public static class AsrResult {
        private Long audioFileId;
        private String recognizedText;
        private String status;
    }
}
