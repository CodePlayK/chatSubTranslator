package com.translate.subtitle.core.service;

import com.alibaba.fastjson.JSON;
import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.entity.openai.config.OpenAiConfig;
import com.translate.subtitle.core.entity.openai.response.ChatGPTResponse;
import com.translate.subtitle.core.util.ChatGPTRequestUtil;
import com.translate.subtitle.core.util.GPTRequestUtil;
import com.translate.subtitle.core.util.localUtils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OpenAiService {
    public static final String OPEN_MARK = "「";
    public static final String CLOSE_MARK = "」";
    //public static final String PRE_QUESTION = "";
    public static final int QUESTION_MAX_LENGTH = 1000;
    private final static String ROLE_USER = "user";
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private ChatGPTRequestUtil chatGPTRequestUtil;
    @Autowired
    private GPTRequestUtil gptRequestUtil;
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private OpenAiConfig openAiConfig;

    /**
     * 将所有有可能是由Mark翻译而来的字符转换为Mark
     *
     * @param parse
     * @return
     */
    private static String possibleTranslateMark2Mark(ChatGPTResponse parse) {
        String translated = parse.getChoices().get(0).getMessage().getContent();
        translated = StringUtils.replace(translated, "“", OPEN_MARK);
        translated = StringUtils.replace(translated, "”", CLOSE_MARK);
        translated = StringUtils.replace(translated, "《", OPEN_MARK);
        translated = StringUtils.replace(translated, "》", CLOSE_MARK);
        return translated;
    }

    public ArrayList<Line> translateByChatGPT(Subtitle subtitle) throws IOException {
        List<Line> lines = subtitle.getLine();
        int wordCount = 0;
        ArrayList<Line> linesTmp = new ArrayList<>();
        ArrayList<Line> newLines = new ArrayList<>();
        StringBuilder builder1 = new StringBuilder();
        for (Line line : lines) {
            if (wordCount + line.getWordCount() < QUESTION_MAX_LENGTH && lines.get(lines.size() - 1).getIndex() != line.getIndex()) {
                linesTmp.add(line);
                wordCount += line.getWordCount();
            } else {
                if (lines.get(lines.size() - 1).getIndex() == line.getIndex()) {
                    linesTmp.add(line);
                }
                String translateLines = "";
                for (int i = 0; i < 5; i++) {
                    StringBuilder builder = new StringBuilder();
                    translateLines = translateLines(linesTmp);
                    translateLines = errorMark2quotationMark(translateLines);
                    List<String> translateLinelist = Arrays.asList(translateLines.split(OPEN_MARK));
                    translateLinelist = fileUtil.delMultiEmptyLine(translateLinelist);
                    test(translateLinelist, linesTmp);
                    int i1 = 0;
                    for (Line line1 : linesTmp) {
                        if (null != line1.getTranslation()) {
                            i1++;
                        } else {
                            builder.append(line1.getIndex()).append(",");
                        }
                    }
                    if (i1 >= linesTmp.size() * 0.75 && i1 != linesTmp.size()) {
                        LOGGER.warn("本段翻译有空行但未触发重试线：缺失行数:[{}],缺失index：[{}]", linesTmp.size() - i1, builder.toString());
                        builder1.append(builder);
                        break;
                    } else if (i1 == linesTmp.size()) {
                        LOGGER.warn("该段翻译成功！");
                    } else {
                        LOGGER.warn("本段翻译异常,缺失行数:[{}],缺失index：[{}],重试第{}次", linesTmp.size() - i1, builder.toString(), i + 1);
                    }
                }

                boolean lastNotEmpty = false;
                for (int i = 0; i < linesTmp.size(); i++) {
                    Line line1 = linesTmp.get(i);
                    if (lastNotEmpty && (null == line1.getTranslation() || line1.getTranslation().isEmpty())) {
                        line1.setTranslation(linesTmp.get(i - 1).getTranslation());
                    } else {
                        lastNotEmpty = true;
                    }
                }
                newLines.addAll(linesTmp);
                subtitle.setTranslatedLine(newLines);
                subtitle.setErrorIndex(builder1.toString());
                fileUtil.writeSubtitle2Local(subtitle);
                linesTmp.clear();
                linesTmp.add(line);
                wordCount = 0;
            }
        }

        return newLines;
    }

    /**
     * 将译文中翻译出现与Mark相同的字符替换
     *
     * @param translateLines
     * @return
     */
    private String errorMark2quotationMark(String translateLines) {
        char[] chars = translateLines.toCharArray();
        for (int i1 = 0; i1 < chars.length; i1++) {
            if (chars[i1] == OPEN_MARK.toCharArray()[0] && i1 + 1 < chars.length && !Character.isDigit(chars[i1 + 1])) {
                chars[i1] = '“';
            }
            if (chars[i1] == CLOSE_MARK.toCharArray()[0] && i1 - 1 >= 0 && !Character.isDigit(chars[i1 - 1])) {
                chars[i1] = '”';
            }
        }
        translateLines = new String(chars);
        return translateLines;
    }

    private void test(List<String> translateLinelist, List<Line> lines) {
        for (String s : translateLinelist) {
            if (s.contains(CLOSE_MARK)) {
                String s1 = StringUtils.substringBefore(s, CLOSE_MARK);
                for (Line line : lines) {
                    if (line.getIndex() == Integer.parseInt(s1)) {
                        line.setTranslation(StringUtils.substringAfter(s, CLOSE_MARK));
                    }
                }
            }

        }
    }

    private String translateLines(ArrayList<Line> linesTmp) {
        StringBuilder subtitleTxtBuilder = new StringBuilder();
        for (Line line : linesTmp) {
            subtitleTxtBuilder.append(OPEN_MARK).append(line.getIndex()).append(CLOSE_MARK).append(line.getOriginal());
        }
        String subtitleTxt = subtitleTxtBuilder.toString();
        LOGGER.info("开始翻译原文:[{}]", subtitleTxt);
        ResponseEntity<String> chat = chatGPTRequestUtil.chat(openAiConfig.getChatPreQuestion() + subtitleTxt, ROLE_USER);
        //ResponseEntity<String> chat = gptRequestUtil.send(PRE_QUESTION + subtitleTxt);
        String body = chat.getBody();
        ChatGPTResponse parse = JSON.parseObject(body, ChatGPTResponse.class);
        String translated = possibleTranslateMark2Mark(parse);
        LOGGER.info("翻译后:[{}]", translated);
        return translated;
    }
}
