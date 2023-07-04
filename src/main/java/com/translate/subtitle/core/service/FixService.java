package com.translate.subtitle.core.service;

import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.entity.openai.LineFix;
import com.translate.subtitle.core.entity.openai.config.LineFixConfig;
import com.translate.subtitle.core.util.localUtils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FixService {
    private static final String FILENAME_PREFIX = "FIX";
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private SubtitleService subtitleService;
    @Autowired
    private LineFixConfig lineFixConfig;
    @Autowired
    private FileUtil fileUtil;

    public void fix() {
        Subtitle subtitle = subtitleService.getSubtitle(lineFixConfig.getFixFileName(), FILENAME_PREFIX);
        fixDislocation(subtitle);
        try {
            fileUtil.writeSubtitle2Local(subtitle);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void fixDislocation(Subtitle subtitle) {
        List<Line> lines = subtitle.getLines();
        List<LineFix> lineFixs = lineFixConfig.getLineFixs();
        for (LineFix lineFix : lineFixs) {
            StringBuilder builder = new StringBuilder();
            List<String> transList = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                transList.add(lines.get(i).getTranslation());
                if (lines.get(i).getIndex() == lineFix.getDislocationIndexOpen()) {
                    lineFix.setListIndexOpen(i);
                }
                if (lines.get(i).getIndex() == lineFix.getDislocationIndexClose()) {
                    lineFix.setListIndexClose(i);
                }
            }
            //将丢失的翻译存到边界
            if (lineFix.getMoveCount() > 0) {
                for (int i = lineFix.getListIndexClose() - lineFix.getMoveCount() + 1; i <= lineFix.getListIndexClose(); i++) {
                    builder.append("@").append(lines.get(i).getTranslation());
                }
            } else {
                for (int i = lineFix.getListIndexOpen(); i <= lineFix.getListIndexOpen() - lineFix.getMoveCount(); i++) {
                    builder.append("@").append(lines.get(i).getTranslation());
                }
            }

            for (int i = lineFix.getListIndexOpen(); i <= lineFix.getListIndexClose(); i++) {
                LOGGER.info("替换:原译文移动{}位,[{}]{}-->", lineFix.getMoveCount(), lines.get(i).getIndex(), lines.get(i).getTranslation());
                lines.get(i).setTranslation(transList.get(i - lineFix.getMoveCount()));
                LOGGER.info("新译文[{}]{}", lines.get(i).getIndex(), lines.get(i).getTranslation());

            }
            if (lineFix.getMoveCount() > 0) {
                lines.get(lineFix.getListIndexClose()).setTranslation(lines.get(lineFix.getListIndexClose()).getTranslation() + builder);
            } else {
                lines.get(lineFix.getListIndexOpen()).setTranslation(builder.toString());
            }
            subtitle.setNewLines(lines);
        }
    }

}
