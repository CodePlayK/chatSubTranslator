package com.translate.subtitle.core.util;

import com.translate.subtitle.core.entity.openai.config.OpenAiConfig;
import com.translate.subtitle.core.entity.whisper.WhisperRequest;
import com.translate.subtitle.core.util.webUtils.CookieUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Proxy;

@Component
public class WhisperRequestUtil {
    private final static String ROLE_USER = "user";
    private final static int WAIT_TIME = 30000;
    private final static int RETRY_TIME = 1;
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private OpenAiConfig openAiConfig;

    public ResponseEntity<String> whisper(File file) throws FileNotFoundException {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, CookieUtils.inetSocketAddress));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);

        HttpHeaders whisperHeader = openAiConfig.getWhisperHeader();
        WhisperRequest whisperRequest = new WhisperRequest();
        FileSystemResource fileSystemResource = new FileSystemResource(file);
        whisperRequest.setFile(fileSystemResource);
        whisperRequest.setModel(openAiConfig.getAudioModel());
        whisperRequest.setLanguage("en");
        whisperRequest.setResponseFormat("srt");
        MultiValueMap body = whisperRequest.getBody();
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, whisperHeader);

        ResponseEntity<String> stringResponseEntity = null;
        for (int i = 0; i < RETRY_TIME; i++) {
            try {
                stringResponseEntity = restTemplate.postForEntity(openAiConfig.getWhisperUrl(), httpEntity, String.class);
                return stringResponseEntity;
            } catch (RestClientException e) {
                LOGGER.warn("请求异常，等待{}s后重试……", WAIT_TIME / 1000, e);
            }
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}
