package com.translate.subtitle.core.entity.openai.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class Usage implements Serializable {

    private String promptTokens;

    private String completionTokens;

    private String totalTokens;
}
