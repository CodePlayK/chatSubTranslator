package com.translate.subtitle.core.entity.openai.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Configuration
@ConfigurationProperties
@Data
public class OpenAiConfig {

    @Value("${openai-service.api-key}")
    private String apiKey;

    @Value("${openai-service.gpt-model}")
    private String model;

    @Value("${openai-service.audio-model}")
    private String audioModel;
    @Value("${openai-service.gpt-model-text}")
    private String gptModel;
    @Value("${openai-service.chatGPT-translator.chat-url}")
    private String chatUrl;
    @Value("${openai-service.GPT-translator.gpt-url}")
    private String gptUrl;
    @Value("${openai-service.whisper.whisper-url}")
    private String whisperUrl;
    @Value("${openai-service.urls.model-url}")
    private String modelUrl;
    @Value("${openai-service.chatGPT-translator.file-name}")
    private String fileName;
    @Value("${openai-service.whisper.audio-file-name}")
    private String audioFileName;
    @Value("${openai-service.chatGPT-translator.question.chat-pre-prompt}")
    private String chatPrePrompt;
    @Value("${openai-service.chatGPT-translator.start-index}")
    private int startIndex;
    @Value("${openai-service.chatGPT-translator.line-retry-exponent}")
    private float retryExponent;
    @Value("${openai-service.chatGPT-translator.line-retry-times}")
    private int lineRetryTimes;
    @Value("${openai-service.chatGPT-translator.question.max-length}")
    private int questionMaxLength;

    public HttpHeaders getChatGPTHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + apiKey);
        return headers;
    }

    public HttpHeaders getWhisperHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", "Bearer " + apiKey);
        return headers;
    }

}
