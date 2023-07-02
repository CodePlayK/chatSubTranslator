package com.translate.subtitle.core.util;

import com.alibaba.fastjson.JSONObject;
import com.translate.subtitle.core.entity.openai.config.OpenAiConfig;
import com.translate.subtitle.core.entity.openai.request.ChatRequest;
import com.translate.subtitle.core.entity.openai.request.GptRequest;
import com.translate.subtitle.core.util.webUtils.RequestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GPTRequestUtil {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private OpenAiConfig openAiConfig;
    @Autowired
    private RequestUtils requestUtils;

    public ResponseEntity<String> send(String subtitlTxt) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setQuestion(subtitlTxt);
        GptRequest gptRequest = new GptRequest();
        gptRequest.setModel(openAiConfig.getGptModel());
        gptRequest.setPrompt(subtitlTxt);
        JSONObject body = gptRequest.getBody();
        HttpHeaders headers = openAiConfig.getChatGPTHeader();
        return requestUtils.request(openAiConfig.getGptUrl(), body, headers);
    }


}
