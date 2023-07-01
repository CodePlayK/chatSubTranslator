package com.translate.subtitle.core.service;

import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.entity.openai.config.ChatGPTConfig;
import com.translate.subtitle.core.util.LineUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LineService {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());

    @Autowired
    private LineUtil lineUtil;
    @Autowired
    private ChatGPTConfig chatGPTConfig;

    public List<Line> getLines(Subtitle subtitle) {
        List<String> txt = subtitle.getTxt();
        List<Line> lines = new ArrayList<>();
        List<String> lineTxt = txt.subList(subtitle.getStartLineNum(), txt.size());
        int index = 1;
        int jumpLineCount = 1;
        switch (subtitle.getType()) {
            case TRANSLATE_NO_INDEX:
                for (int i = 0; i < lineTxt.size(); i += 4) {
                    index = withIndex(subtitle, lines, lineTxt, index, jumpLineCount, i);
                }
                break;
            case TRANSLATE_INDEX:
                jumpLineCount = 2;
                for (int i = 0; i < lineTxt.size(); i += 5) {
                    Line line = new Line();
                    line.setIndex(chatGPTConfig.getStartIndex() + index++);
                    line.setTimestamp(lineTxt.get(i + 1));
                    if (subtitle.isTranslationFirstinLine()) {
                        line.setTranslation(lineTxt.get(i + jumpLineCount));
                        line.setOriginal(lineTxt.get(i + jumpLineCount + 1));
                    } else {
                        line.setOriginal(lineTxt.get(i + jumpLineCount));
                        line.setTranslation(lineTxt.get(i + jumpLineCount + 1));
                    }
                    lines.add(line);
                }
                break;
            case NO_TRANSLATE_INDEX:
                jumpLineCount = 2;
                for (int i = 0; i < lineTxt.size(); i += 4) {
                    try {
                        Line line = new Line();
                        line.setIndex(chatGPTConfig.getStartIndex() + index++);
                        line.setTimestamp(lineTxt.get(i + 1));
                        line.setOriginal(lineTxt.get(i + jumpLineCount));
                        lines.add(line);
                    } catch (Exception e) {
                        LOGGER.error("逐行解析异常！最后一行原文为：{}", lines.get(lines.size() - 1).getOriginal());
                    }
                }
                break;
            case NO_TRANSLATE_NO_INDEX:
                for (int i = 0; i < lineTxt.size(); i += 3) {
                    index = noIndex(lines, lineTxt, index, jumpLineCount, i);
                }
        }
        for (Line line : lines) {
            line.setLineTxt(getLineTxt(line));
            line.setWordCount(line.getLineTxt().length());
            line.setOriginal(StringUtils.replace(line.getOriginal(), OpenAiService.CUT_MARK, ""));
            line.setOriginal(StringUtils.replace(line.getOriginal(), OpenAiService.NUM_MARK, ""));
        }
        subtitle.setLine(lines);
        return lines;
    }

    String getLineTxt(Line line) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(line.getIndex()).append("#")
        //.append("\r\n")
        //.append(line.getTimestamp())
        ;
        if (null != line.getTranslation()) {
            stringBuilder.append("\r\n").append(line.getTranslation());
        }
        stringBuilder.append("\r\n").append(line.getOriginal()).append("\r\n").append("\r\n");
        return stringBuilder.toString();
    }


    private int noIndex(List<Line> lines, List<String> lineTxt, int index, int jumpLineCount, int i) {
        try {
            Line line = new Line();
            line.setIndex(chatGPTConfig.getStartIndex() + index++);
            line.setTimestamp(lineTxt.get(i));
            line.setOriginal(lineTxt.get(i + jumpLineCount));
            lines.add(line);
        } catch (Exception e) {
            LOGGER.error("逐行解析异常！最后一行原文为：{}", lines.get(lines.size() - 1).getOriginal());
        }
        return index;
    }

    private int withIndex(Subtitle subtitle, List<Line> lines, List<String> lineTxt, int index, int jumpLineCount, int i) {
        Line line = new Line();
        line.setIndex(chatGPTConfig.getStartIndex() + index++);
        line.setTimestamp(lineTxt.get(i));
        if (subtitle.isTranslationFirstinLine()) {
            line.setTranslation(lineTxt.get(i + jumpLineCount));
            line.setOriginal(lineTxt.get(i + jumpLineCount + 1));
        } else {
            line.setOriginal(lineTxt.get(i + jumpLineCount));
            line.setTranslation(lineTxt.get(i + jumpLineCount + 1));
        }
        lines.add(line);
        return index;
    }
}
