package com.qiniu.voiceinput.config;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfig {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain;
    private AsrConfig asr;

    @Data
    public static class AsrConfig {
        private String url;
    }

    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }

    @Bean
    public UploadManager uploadManager() {
        com.qiniu.storage.Configuration cfg = new com.qiniu.storage.Configuration(Region.region0());
        return new UploadManager(cfg);
    }

    @Bean
    public BucketManager bucketManager(Auth auth) {
        com.qiniu.storage.Configuration cfg = new com.qiniu.storage.Configuration(Region.region0());
        return new BucketManager(auth, cfg);
    }
}
