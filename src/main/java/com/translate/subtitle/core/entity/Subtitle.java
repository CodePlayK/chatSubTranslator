package com.translate.subtitle.core.entity;

import lombok.Data;

import java.util.List;

@Data
public class Subtitle {
    private SubtitleType type;
    private boolean indexNumFlag;
    private List<Line> line;
    private List<Line> translatedLine;
    private boolean translated;
    private int startLineNum;

    private String head;
    private boolean translationFirstinLine;
    private List<String> txt;
    private int startIndex;


}
