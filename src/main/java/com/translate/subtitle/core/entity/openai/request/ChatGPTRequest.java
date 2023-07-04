package com.translate.subtitle.core.entity.openai.request;

import com.alibaba.fastjson.JSONObject;
import com.translate.subtitle.core.entity.openai.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatGPTRequest implements Serializable {
    private String model;
    private List<Message> messages;

    public JSONObject getBody() {
        JSONObject param = new JSONObject();
        param.put("model", model);
        param.put("messages", messages);
        param.put("temperature", 1.2);
        return param;
    }
}
