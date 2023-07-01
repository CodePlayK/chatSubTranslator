package com.translate.subtitle.core.service;

import com.alibaba.fastjson.JSON;
import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.entity.openai.config.ChatGPTConfig;
import com.translate.subtitle.core.entity.openai.response.ChatGPTResponse;
import com.translate.subtitle.core.util.ChatGPTRequestUtil;
import com.translate.subtitle.core.util.FileUtil;
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
    public static final String CUT_MARK = "「";
    public static final String NUM_MARK = "」";
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    String test = "1#\n" +
            "[音乐]\n" +
            "\n" +
            "2#\n" +
            "《童话之旅》是那种难以忘怀的冒险之一。\n" +
            "\n" +
            "3#\n" +
            "我知道在几个月后我还会记得这款游戏，我现在就想开始。\n" +
            "\n" +
            "4#\n" +
            "今年的发行中，这是又一款几乎所有内容都糟糕或设计混乱的游戏，让十二月组织年度游戏候选名单变得非常容易。\n" +
            "\n" +
            "5#\n" +
            "我要给史克威尔艾尼克斯点赞，他们跳出了那些尖头发的日本动漫角色的舒适区，一次选择了真实世界中的人类，\n" +
            "\n" +
            "6#\n" +
            "但然后我又倾向于把他们推回去，因为这显然是个错误。\n" +
            "\n" +
            "7#\n" +
            "史克威尔艾尼克斯需要被隔离，就像是一种具有破坏创造力的细菌病毒。\n" +
            "\n" +
            "8#\n" +
            "他们试图制作一部符合他们想象中西方市场潮流的故事，却没有灵魂，就像是跟你自己的倒影对话一样。\n" +
            "\n" +
            "9#\n" +
            "在当代叙事中，最具威胁和可憎的力量通常是现代的诽谤、父权制和资本主义。\n" +
            "\n" +
            "10#\n" +
            "对于一些人来说，战胜奇幻世界中的魔鬼领主似乎比在现实世界中航行更容易。\n" +
            "\n" +
            "11#\n" +
            "如果这个情景让你感到困惑，那就准备好了。\n" +
            "\n" +
            "12#\n" +
            "现在是时候进入受欢迎的日本文学流派 - 异世界，相当于反复加热同样一块冷冻披萨。\n" +
            "\n" +
            "13#\n" +
            "异世界故事就是把一个现代主人公，通常是一个无望、没有目标的灵魂，送到一个通用的奇幻世界中，在那里他们受到尊敬，\n" +
            "\n" +
            "14#\n" +
            "拯救世界，并拥有一群崇拜他们那个没响的闹钟级别魅力的女人的后宫。\n" +
            "\n" +
            "15#\n" +
            "忘记心理治疗和自我提升，成为最好的自己只需要一个充满混乱和危险的世界，再加上一些不会让周围所有人疏远的神级力量。";
    @Autowired
    private ChatGPTRequestUtil chatGPTRequestUtil;
    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private ChatGPTConfig chatGPTConfig;

    public ArrayList<Line> translateByChatGPT(Subtitle subtitle) throws IOException {
        List<Line> lines = subtitle.getLine();
        int wordCount = 0;
        ArrayList<Line> linesTmp = new ArrayList<>();
        ArrayList<Line> newLines = new ArrayList<>();
        for (Line line : lines) {
            if (wordCount + line.getWordCount() < 1500 && lines.get(lines.size() - 1).getIndex() != line.getIndex()) {
                linesTmp.add(line);
                wordCount += line.getWordCount();
            } else {
                if (lines.get(lines.size() - 1).getIndex() == line.getIndex()) {
                    linesTmp.add(line);
                }
                String translateLines = "";
                for (int i = 0; i < 3; i++) {
                    translateLines = translateLines(linesTmp);
                    //String translateLines = test;
                    char[] chars = translateLines.toCharArray();
                    for (int i1 = 0; i1 < chars.length; i1++) {
                        if (chars[i1] == '「' && i1 + 1 < chars.length && !Character.isDigit(chars[i1 + 1])) {
                            chars[i1] = '“';
                        }
                        if (chars[i1] == '」' && i1 - 1 >= 0 && !Character.isDigit(chars[i1 - 1])) {
                            chars[i1] = '”';
                        }
                    }
                    translateLines = new String(chars);
                    List<String> translateLinelist = Arrays.asList(translateLines.split(CUT_MARK));
                    translateLinelist = fileUtil.delMultiEmptyLine(translateLinelist);
                    test(translateLinelist, linesTmp);
                    if (null != linesTmp.get(0).getTranslation()) {
                        break;
                    } else {
                        LOGGER.warn("本段翻译异常，重试第{}次", i + 1);
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
                fileUtil.writeSubtitle2Local(subtitle);
                linesTmp.clear();
                linesTmp.add(line);
                wordCount = 0;
            }
        }
        return newLines;
    }

    private void test(List<String> translateLinelist, List<Line> lines) {
        for (String s : translateLinelist) {
            if (s.contains(NUM_MARK)) {
                String s1 = StringUtils.substringBefore(s, NUM_MARK);
                for (Line line : lines) {
                    if (line.getIndex() == Integer.parseInt(s1)) {
                        line.setTranslation(StringUtils.substringAfter(s, NUM_MARK));
                    }
                }
            }

        }
    }

    private String translateLines(ArrayList<Line> linesTmp) {
        String translateHead = "将英文翻译成中文,每一句话尽量短:\r\n";
        StringBuilder subtitleTxtBuilder = new StringBuilder();
        for (Line line : linesTmp) {
            //subtitleTxtBuilder.append(line.getLineTxt());
            subtitleTxtBuilder.append(CUT_MARK).append(line.getIndex()).append(NUM_MARK).append(line.getOriginal());
        }
        String subtitleTxt = subtitleTxtBuilder.toString();
        LOGGER.info("开始翻译原文:[{}]", subtitleTxt);
        ResponseEntity<String> chat = chatGPTRequestUtil.chat(translateHead + subtitleTxt);
        String body = chat.getBody();
        ChatGPTResponse parse = JSON.parseObject(body, ChatGPTResponse.class);
        String translated = parse.getChoices().get(0).getMessage().getContent();
        translated = StringUtils.replace(translated, "“", "「");
        translated = StringUtils.replace(translated, "”", "」");
        translated = StringUtils.replace(translated, "《", "「");
        translated = StringUtils.replace(translated, "》", "」");
        LOGGER.info("翻译后:[{}]", translated);
        return translated;
    }
}
