package com.translate.subtitle.core.entity;

import lombok.Data;

@Data
public class Line {
    private int index;
    private String timestamp;
    private String original;
    private String translation;
    private int wordCount;

    private String lineTxt;


}
