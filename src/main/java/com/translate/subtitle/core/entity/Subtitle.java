package com.translate.subtitle.core.entity;

import lombok.Data;

import java.util.List;

@Data
public class Subtitle {
    private SubtitleType type;
    private boolean indexNumFlag;
    private List<Line> lines;
    private List<Line> newLines;
    private boolean translated;
    private int startLineNum;
    private int openTimeStampLineNum;
    private String head;
    private String errorIndex;
    private boolean translationFirstinLine;
    private List<String> txt;
    private int startIndex;


}
