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
        System.out.println();
    }

    private void fixDislocation(Subtitle subtitle) {
        List<Line> lines = subtitle.getLines();
        List<LineFix> lineFixs = lineFixConfig.getLineFixs();
        for (LineFix lineFix : lineFixs) {
            int dislocationIndexOpen = lineFix.getDislocationIndexOpen();

            int dislocationIndexClose = lineFix.getDislocationIndexClose();
            if (lineFix.getMoveCount() > 0) {
                StringBuilder builder = new StringBuilder();

                for (int i = lines.size() - 1; i >= 0; i--) {
                    Line line = lines.get(i);
                    if (line.getIndex() >= dislocationIndexOpen
                            && line.getIndex() <= dislocationIndexClose) {
                        if (line.getIndex() > dislocationIndexClose - lineFix.getMoveCount()) {
                            builder.append("@").append("[").append(line.getIndex()).append("]").append(line.getTranslation());
                        }
                        moveLines(lines, lineFix, dislocationIndexClose, i, line);

                    }
                }
                for (Line line : lines) {
                    if (line.getIndex() == dislocationIndexClose) {
                        line.setTranslation(line.getTranslation() + builder);
                    }
                }
            } else {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < lines.size(); i++) {
                    Line line = lines.get(i);
                    if (line.getIndex() >= dislocationIndexOpen
                            && line.getIndex() <= dislocationIndexClose) {

                        if (line.getIndex() < dislocationIndexOpen - lineFix.getMoveCount()) {
                            builder.append("@").append("[").append(line.getIndex()).append("]").append(line.getTranslation());
                        }

                        moveLines(lines, lineFix, dislocationIndexOpen, i, line);
                    }
                }
                for (Line line : lines) {
                    if (line.getIndex() == dislocationIndexOpen) {
                        line.setTranslation(line.getTranslation() + builder);
                    }
                }
            }
            subtitle.setNewLines(lines);
        }
    }

    private void moveLines(List<Line> lines, LineFix lineFix, int dislocationIndexOpen, int i, Line line) {
        String translation = lines.get(i - lineFix.getMoveCount()).getTranslation();
        LOGGER.info("替换:原译文移动{}位,[{}]{}-->", lineFix.getMoveCount(), line.getIndex(), line.getTranslation());
        line.setTranslation(translation);
        LOGGER.info("新译文[{}]{}-->", line.getIndex(), line.getTranslation());
    }
}
