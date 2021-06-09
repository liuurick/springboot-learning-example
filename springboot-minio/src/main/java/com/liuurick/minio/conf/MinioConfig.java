package com.liuurick.minio.conf;

import com.liuurick.minio.exception.CustomException;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minio客户端配置
 */
@Slf4j
@Configuration
public class MinioConfig {

    @Autowired
    private MinioData minioData;

    /**
     * 初始化minio客户端,不用每次都初始化
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        try {
            return new MinioClient(minioData.getUrl(), minioData.getAccessKey(), minioData.getSecretKey());
        }
        catch (final Exception e) {
            log.error("初始化minio出现异常:{}", e.fillInStackTrace());
            throw new CustomException("初始化minio出现异常");
        }
    }

}