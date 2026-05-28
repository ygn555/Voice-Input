package com.qiniu.voiceinput.service;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.voiceinput.config.QiniuConfig;
import com.qiniu.voiceinput.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QiniuService {

    private final Auth auth;
    private final UploadManager uploadManager;
    private final BucketManager bucketManager;
    private final QiniuConfig qiniuConfig;

    /**
     * 上传文件到七牛云
     */
    public UploadResult uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String key = generateFileKey(originalFilename);

        try {
            String upToken = auth.uploadToken(qiniuConfig.getBucket());
            byte[] fileBytes = file.getBytes();

            Response response = uploadManager.put(fileBytes, key, upToken);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

            String url = String.format("http://%s/%s", qiniuConfig.getDomain(), putRet.key);

            log.info("文件上传成功: key={}, url={}", putRet.key, url);

            return UploadResult.builder()
                    .key(putRet.key)
                    .hash(putRet.hash)
                    .url(url)
                    .fileName(originalFilename)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .build();

        } catch (QiniuException e) {
            log.error("七牛云上传失败: {}", e.response.toString(), e);
            throw new BusinessException("文件上传失败: " + e.response.error);
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new BusinessException("读取文件失败");
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String key) {
        try {
            bucketManager.delete(qiniuConfig.getBucket(), key);
            log.info("文件删除成功: key={}", key);
        } catch (QiniuException e) {
            log.error("七牛云删除文件失败: key={}", key, e);
            throw new BusinessException("删除文件失败: " + e.response.error);
        }
    }

    /**
     * 获取文件信息
     */
    public com.qiniu.storage.model.FileInfo getFileInfo(String key) {
        try {
            return bucketManager.stat(qiniuConfig.getBucket(), key);
        } catch (QiniuException e) {
            log.error("获取文件信息失败: key={}", key, e);
            throw new BusinessException("获取文件信息失败");
        }
    }

    /**
     * 生成唯一文件key
     */
    private String generateFileKey(String originalFilename) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return String.format("audio/%s%s", uuid, extension);
    }

    @lombok.Data
    @lombok.Builder
    public static class UploadResult {
        private String key;
        private String hash;
        private String url;
        private String fileName;
        private Long fileSize;
        private String mimeType;
    }
}
