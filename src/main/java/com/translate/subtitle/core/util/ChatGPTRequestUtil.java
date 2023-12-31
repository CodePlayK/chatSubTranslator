package com.translate.subtitle.core.util;

import com.alibaba.fastjson.JSONObject;
import com.translate.subtitle.core.entity.openai.Message;
import com.translate.subtitle.core.entity.openai.config.OpenAiConfig;
import com.translate.subtitle.core.entity.openai.request.ChatGPTRequest;
import com.translate.subtitle.core.entity.openai.request.ChatRequest;
import com.translate.subtitle.core.util.webUtils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ChatGPTRequestUtil {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private OpenAiConfig openAiConfig;
    @Autowired
    private RequestUtils requestUtils;

    public ResponseEntity<String> chat(String subtitlTxt, String role) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setQuestion(subtitlTxt);
        Message message = new Message();
        message.setRole(role);
        message.setContent(chatRequest.getQuestion());
        ChatGPTRequest chatGPTRequest = ChatGPTRequest.builder()
                .model(openAiConfig.getModel())
                .messages(Collections.singletonList(message))
                .build();
        HttpHeaders headers = openAiConfig.getChatGPTHeader();
        JSONObject body = chatGPTRequest.getBody();
        return requestUtils.request(openAiConfig.getChatUrl(), body, headers);
    }

}
