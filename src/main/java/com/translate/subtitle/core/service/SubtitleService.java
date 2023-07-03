package com.translate.subtitle.core.service;

import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.entity.SubtitleType;
import com.translate.subtitle.core.entity.openai.config.OpenAiConfig;
import com.translate.subtitle.core.util.ChatGPTRequestUtil;
import com.translate.subtitle.core.util.localUtils.FileUtil;
import com.translate.subtitle.core.util.localUtils.LineUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubtitleService {
    private static final String SYSTEM = "system";
    private static final String ASSISTANT = "assistant";
    private static final String TIMESTAMP_MARK = "-->";
    private static final String FILENAME_PREFIX = "TRANS";
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private LineUtil lineUtil;
    @Autowired
    private LineService lineService;
    @Autowired
    private OpenAiService openAiService;
    @Autowired
    private ChatGPTRequestUtil chatGPTRequestUtil;
    @Autowired
    private OpenAiConfig openAiConfig;

    public void translate() {
        preRun();
        Subtitle subtitle = getSubtitle(openAiConfig.getFileName(), FILENAME_PREFIX);
        ArrayList<Line> chatGPTLines = null;
        try {
            chatGPTLines = openAiService.translateByChatGPT(subtitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        subtitle.setNewLines(chatGPTLines);
        try {
            fileUtil.writeSubtitle2Local(subtitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
    }

    public Subtitle getSubtitle(String fileName, String prefix) {
        File file = new File(fileName);
        String name = file.getName();
        String parent = file.getParent();
        String newFullPath = String.format("%s\\[%s]%s", parent, prefix, name);
        List<String> txt = fileUtil.readFile(fileName, newFullPath);
        Subtitle subtitle = getSubtitleWithFormat(txt);
        List<Line> lines = lineService.getLines(subtitle);
        subtitle.setLines(lines);
        checkLines(subtitle);
        logOriginSubtitle(subtitle);
        return subtitle;
    }

    private Subtitle getSubtitleWithFormat(List<String> txt) {
        Subtitle subtitle = new Subtitle();
        subtitle.setTxt(txt);
        int openTimeStampLineNum = getOpenTimeStampLineNum(txt);
        subtitle.setOpenTimeStampLineNum(openTimeStampLineNum);
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
                    try {
                        if (openAiConfig.getStartIndex() == 0) {
                            String s = txt.get(subtitle.getStartLineNum());
                            int startIndex = Integer.parseInt(s);
                            openAiConfig.setStartIndex(startIndex);
                            LOGGER.info("自动识别首index成功[{}]", openAiConfig.getStartIndex());
                        } else {
                            LOGGER.info("已手动配置首index[{}]", openAiConfig.getStartIndex());
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.warn("自动解析字幕首index失败，且未手动配置首index！请检查源文件！");
                        throw new RuntimeException(e);
                    }
                    openAiConfig.setStartIndex(openAiConfig.getStartIndex() - 1);
                    subtitle.setStartIndex(openAiConfig.getStartIndex());
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
        List<Line> lines = subtitle.getLines();
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
        return lineUtil.isContainChinese(txt.get(subtitle.getOpenTimeStampLineNum() + 1));
    }


    private int getOpenTimeStampLineNum(List<String> txt) {
        for (int i = 0; i < txt.size(); i++) {
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
                , subtitle.getLines().size()
        );
    }


    public void preRun() {
        chatGPTRequestUtil.chat("下面将英文字幕翻译成中文。", SYSTEM);
        //chatGPTRequestUtil.chat("生成的文本按原序号分段。", ASSISTANT);
    }
}

