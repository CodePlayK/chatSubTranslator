package com.translate.subtitle.core.entity.openai.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@ConfigurationProperties
@Data
public class ChatGPTConfig {

    @Value("${openai-service.http-client.read-timeout}")
    private int readTimeout;

    @Value("${openai-service.http-client.connect-timeout}")
    private int connectTimeout;

    @Value("${openai-service.api-key}")
    private String apiKey;

    @Value("${openai-service.gpt-model}")
    private String model;

    @Value("${openai-service.audio-model}")
    private String audioModel;
    @Value("${openai-service.urls.chat-url}")
    private String chatUrl;
    @Value("${openai-service.urls.create-transcription-url}")
    private String transcriptionUrl;
    @Value("${openai-service.urls.model-url}")
    private String modelUrl;
    @Value("${openai-service.file-name}")
    private String fileName;
    @Value("${openai-service.start-index}")
    private int startIndex;

    public HttpHeaders getHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + apiKey);
        return headers;
    }

}
