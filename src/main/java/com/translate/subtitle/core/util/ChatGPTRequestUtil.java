package com.translate.subtitle.core.util;

import com.alibaba.fastjson.JSONObject;
import com.translate.subtitle.core.entity.openai.Message;
import com.translate.subtitle.core.entity.openai.config.ChatGPTConfig;
import com.translate.subtitle.core.entity.openai.request.ChatGPTRequest;
import com.translate.subtitle.core.entity.openai.request.ChatRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.Proxy;
import java.util.Collections;

@Component
public class ChatGPTRequestUtil {
    private final static String ROLE_USER = "user";
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private ChatGPTConfig chatGPTConfig;

    public ResponseEntity<String> chat(String subtitlTxt) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setQuestion(subtitlTxt);
        Message message = new Message();
        message.setRole(ROLE_USER);
        message.setContent(chatRequest.getQuestion());
        ChatGPTRequest chatGPTRequest = ChatGPTRequest.builder()
                .model(chatGPTConfig.getModel())
                .messages(Collections.singletonList(message))
                .build();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = chatGPTConfig.getHeader();
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, CookieUtils.inetSocketAddress));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        JSONObject body = chatGPTRequest.getBody();
        HttpEntity<Object> httpEntity = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> stringResponseEntity = null;
        for (int i = 0; i < 5; i++) {
            try {
                stringResponseEntity = restTemplate.postForEntity(chatGPTConfig.getChatUrl(), httpEntity, String.class);
                return stringResponseEntity;
            } catch (RestClientException e) {
                LOGGER.warn("请求异常，等待30s后重试……", e);
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return stringResponseEntity;
    }

    public ResponseEntity<String> getModels() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = chatGPTConfig.getHeader();
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, CookieUtils.inetSocketAddress));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(chatGPTConfig.getModelUrl(), HttpMethod.GET, httpEntity, String.class);
    }

}
