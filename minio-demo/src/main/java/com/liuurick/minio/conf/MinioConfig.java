package com.liuurick.minio.conf;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

	private String endpoint;

	/**
	 * minio用户名
	 */
	private String accessKey;

	/**
	 * minio密码
	 */
	private String secretKey;


	@Bean
	public MinioClient getMinioClient() throws InvalidEndpointException, InvalidPortException {
		MinioClient minioClient = new MinioClient(
				endpoint, accessKey, secretKey);
		return minioClient;
	}

}
