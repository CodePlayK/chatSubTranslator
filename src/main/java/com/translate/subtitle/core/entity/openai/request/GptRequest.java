package com.translate.subtitle.core.entity.openai.request;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GptRequest {
    private String model;
    private String prompt;

    public JSONObject getBody() {
        JSONObject param = new JSONObject();
        param.put("model", model);
        param.put("prompt", prompt);
        param.put("temperature", 1.2);
        param.put("max_tokens", 3000);
        param.put("stream", false);
        param.put("n", 1);
        return param;
    }
}
