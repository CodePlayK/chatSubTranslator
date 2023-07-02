package com.translate.subtitle.core.entity.openai.response;

import com.translate.subtitle.core.entity.openai.Message;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Choice implements Serializable {
    private Integer index;
    private Message message;
    private List<String> text;
}
