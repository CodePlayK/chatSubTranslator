package com.translate.subtitle.core.entity.whisper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhisperRequest {
    private String model;
    private FileSystemResource file;
    private String prompt;
    private String responseFormat;
    private String language;

    public MultiValueMap getBody() {
        MultiValueMap param = new LinkedMultiValueMap();
        param.add("model", model);
        param.add("file", file);
        param.add("prompt", prompt);
        param.add("response_format", responseFormat);
        param.add("language", language);
        return param;
    }
}
