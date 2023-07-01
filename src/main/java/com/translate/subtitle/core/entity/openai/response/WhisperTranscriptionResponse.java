package com.translate.subtitle.core.entity.openai.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class WhisperTranscriptionResponse implements Serializable {
    private String text;
}
