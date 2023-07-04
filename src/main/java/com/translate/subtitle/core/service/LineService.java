package com.translate.subtitle.core.service;

import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.entity.openai.config.OpenAiConfig;
import com.translate.subtitle.core.util.localUtils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LineService {
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    private final String LINE_BREAKER = "\r\n";
    private final String TIME_MARK = "-->";
    @Autowired
    private OpenAiConfig openAiConfig;
    @Autowired
    private FileUtil fileUtil;


    public List<Line> getLines(Subtitle subtitle) {
        List<String> txt = subtitle.getTxt();
        List<Line> lines = new ArrayList<>();
        List<String> lineTxt = txt.subList(subtitle.getStartLineNum(), txt.size());
        int index = 1;
        int jumpLineCount = 1;
        switch (subtitle.getType()) {
            case TRANSLATE_NO_INDEX:
                for (int i = 0; i < lineTxt.size(); i += 4) {
                    index = translateNoIdex(subtitle, lines, lineTxt, index, jumpLineCount, i);
                }
                break;
            case TRANSLATE_INDEX:
                jumpLineCount = 2;
                for (int i = 0; i < lineTxt.size(); i += 5) {
                    Line line = new Line();
                    line.setIndex(Integer.parseInt(lineTxt.get(i)));
                    line.setTimestamp(lineTxt.get(i + 1));
                    getFormat(subtitle, lines, lineTxt, jumpLineCount, i, line);
                }
                break;
            case NO_TRANSLATE_INDEX:
                lineTxt.add("");
                lineTxt.add(TIME_MARK);
                jumpLineCount = 2;
                int lastMarki = subtitle.getOpenTimeStampLineNum();
                StringBuilder builder = new StringBuilder();
                for (int i = subtitle.getStartLineNum(); i < lineTxt.size(); i++) {
                    String s = lineTxt.get(i);
                    if (i == lineTxt.size() - 1) {

                    }
                    if (s.contains(TIME_MARK)) {
                        for (int j = lastMarki + 1; j < i - 1; j++) {
                            if (!lineTxt.get(j).isEmpty()) {
                                builder.append(lineTxt.get(j)).append(" ");
                                lineTxt.set(j, "");
                            } else {
                                lineTxt.set(lastMarki + 1, builder.toString());
                                builder.delete(0, builder.length());
                                lastMarki = i;
                                break;
                            }
                        }
                    }
                }
                lineTxt = fileUtil.delMultiEmptyLine(lineTxt);
                lineTxt.remove(lineTxt.size() - 1);

                for (int i = 0; i < lineTxt.size(); i += 4) {
                    String s = lineTxt.get(i);
                    try {
                        Line line = new Line();
                        line.setIndex(Integer.parseInt(s));
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
                    index = noTranslateNoIndex(lines, lineTxt, index, jumpLineCount, i);
                }
        }
        for (Line line : lines) {
            line.setLineTxt(getLineTxt(line));
            line.setWordCount(line.getLineTxt().length());
            line.setOriginal(StringUtils.replace(line.getOriginal(), OpenAiService.OPEN_MARK, ""));
            line.setOriginal(StringUtils.replace(line.getOriginal(), OpenAiService.CLOSE_MARK, ""));
        }
        subtitle.setLines(lines);
        lines = sortLines(lines);
        return lines;
    }

    private void getFormat(Subtitle subtitle, List<Line> lines, List<String> lineTxt, int jumpLineCount, int i, Line line) {
        if (subtitle.isTranslationFirstinLine()) {
            line.setTranslation(lineTxt.get(i + jumpLineCount));
            line.setOriginal(lineTxt.get(i + jumpLineCount + 1));
        } else {
            line.setOriginal(lineTxt.get(i + jumpLineCount));
            line.setTranslation(lineTxt.get(i + jumpLineCount + 1));
        }
        lines.add(line);
    }

    private List<Line> sortLines(List<Line> lines) {
        lines = lines.stream().sorted(Comparator.comparing(Line::getIndex)).collect(Collectors.toList());
        return lines;
    }

    String getLineTxt(Line line) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OpenAiService.OPEN_MARK).append(line.getIndex()).append(OpenAiService.CLOSE_MARK);
        if (null != line.getTranslation()) {
            stringBuilder.append(LINE_BREAKER).append(line.getTranslation());
        }
        stringBuilder.append(LINE_BREAKER).append(line.getOriginal()).append(LINE_BREAKER).append(LINE_BREAKER);
        return stringBuilder.toString();
    }


    private int noTranslateNoIndex(List<Line> lines, List<String> lineTxt, int index, int jumpLineCount, int i) {
        try {
            Line line = new Line();
            line.setIndex(openAiConfig.getStartIndex() + index++);
            line.setTimestamp(lineTxt.get(i));
            line.setOriginal(lineTxt.get(i + jumpLineCount));
            lines.add(line);
        } catch (Exception e) {
            LOGGER.error("逐行解析异常！最后一行原文为：{}", lines.get(lines.size() - 1).getOriginal());
        }
        return index;
    }

    private int translateNoIdex(Subtitle subtitle, List<Line> lines, List<String> lineTxt, int index, int jumpLineCount, int i) {
        Line line = new Line();
        line.setIndex(openAiConfig.getStartIndex() + index++);
        line.setTimestamp(lineTxt.get(i));
        getFormat(subtitle, lines, lineTxt, jumpLineCount, i, line);
        return index;
    }
}
