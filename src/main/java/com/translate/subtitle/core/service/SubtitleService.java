package com.translate.subtitle.core.service;

import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.entity.SubtitleType;
import com.translate.subtitle.core.entity.openai.config.ChatGPTConfig;
import com.translate.subtitle.core.util.FileUtil;
import com.translate.subtitle.core.util.LineUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubtitleService {
    private static final String TIMESTAMP_MARK = "-->";
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private LineUtil lineUtil;
    @Autowired
    private LineService lineService;
    @Autowired
    private TranslateService translateService;
    @Autowired
    private OpenAiService openAiService;
    @Autowired
    private ChatGPTConfig chatGPTConfig;

    public void process() {
        List<String> txt = fileUtil.readFile(chatGPTConfig.getFileName());
        Subtitle subtitle = getSubtitleWithFormat(txt);
        List<Line> lines = lineService.getLines(subtitle);
        subtitle.setLine(lines);
        checkLines(subtitle);
        logOriginSubtitle(subtitle);
        //translateService.translateLineByGoogle(subtitle);
        ArrayList<Line> chatGPTLines = null;
        try {
            chatGPTLines = openAiService.translateByChatGPT(subtitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        subtitle.setTranslatedLine(chatGPTLines);
        try {
            fileUtil.writeSubtitle2Local(subtitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
    }

    private Subtitle getSubtitleWithFormat(List<String> txt) {
        Subtitle subtitle = new Subtitle();
        subtitle.setTxt(txt);
        int openTimeStampLineNum = getOpenTimeStampLineNum(txt);
        subtitle.setTranslated(txt.size() > openTimeStampLineNum + 2 && !txt.get(openTimeStampLineNum + 2).isEmpty());
        if (subtitle.isTranslated()) {
            subtitle.setIndexNumFlag(!txt.get(openTimeStampLineNum + 4).contains(TIMESTAMP_MARK));
        } else {
            subtitle.setIndexNumFlag(!txt.get(openTimeStampLineNum + 3).contains(TIMESTAMP_MARK));
        }
        List<String> head = new ArrayList<>();
        for (int i = 0; i <= openTimeStampLineNum; i++) {
            if (txt.get(i).contains(TIMESTAMP_MARK)) {
                if (subtitle.isIndexNumFlag()) {
                    subtitle.setStartLineNum(i - 1);
                } else {
                    subtitle.setStartLineNum(i);
                }
            } else {
                head.add(txt.get(i));
            }
        }
        if (subtitle.isTranslated() && subtitle.isIndexNumFlag()) {
            subtitle.setType(SubtitleType.TRANSLATE_INDEX);
        } else if (!subtitle.isTranslated() && subtitle.isIndexNumFlag()) {
            subtitle.setType(SubtitleType.NO_TRANSLATE_INDEX);
        } else if (!subtitle.isTranslated()) {
            subtitle.setType(SubtitleType.NO_TRANSLATE_NO_INDEX);
        } else {
            subtitle.setType(SubtitleType.TRANSLATE_NO_INDEX);
        }
        subtitle.setTranslationFirstinLine(getTranslationFirst(subtitle));
        return subtitle;
    }

    private void checkLines(Subtitle subtitle) {
        List<Line> lines = subtitle.getLine();
        for (int i = 0; i < lines.size(); i++) {
            if ((null != lines.get(i).getOriginal() && lines.get(i).getOriginal().contains(TIMESTAMP_MARK))
                    || (null != lines.get(i).getTranslation() && lines.get(i).getTranslation().contains(TIMESTAMP_MARK))) {
                LOGGER.error("当前译文或原文中包含时间戳关键字,可能存在错行情况,上一行正确的原文为:{}", lines.get(i - 1).getOriginal());
                break;
            }
        }
    }

    private boolean getTranslationFirst(Subtitle subtitle) {
        List<String> txt = subtitle.getTxt();
        return lineUtil.isContainChinese(txt.get(subtitle.getStartLineNum() + 1));
    }


    private int getOpenTimeStampLineNum(List<String> txt) {
        for (int i = 1; i <= txt.size(); i++) {
            if (txt.get(i).contains(TIMESTAMP_MARK) && !txt.get(i + 1).isEmpty()) {
                return i;
            }
        }
        return 0;
    }

    private void logOriginSubtitle(Subtitle subtitle) {
        LOGGER.info("解析结束:字幕类型[{}]," +
                        "包含数标[{}]," +
                        "头部内容:[{}]," +
                        "译文[{}]," +
                        "译文在上[{}]," +
                        "正文起始行数[{}]," +
                        "总句数[{}]"
                , subtitle.getType()
                , subtitle.isIndexNumFlag()
                , subtitle.getHead()
                , subtitle.isTranslated()
                , subtitle.isTranslationFirstinLine()
                , subtitle.getStartLineNum()
                , subtitle.getLine().size()
        );
    }
}

