package com.translate.subtitle.core.util;

import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.entity.openai.config.ChatGPTConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileUtil {
    private static String ORIGIN_PATH;
    private static String FILE_NAME;
    private static String NEW_FULL_PATH;
    private static String ORIGIN_FULL_PATH;
    @Autowired
    private ChatGPTConfig chatGPTConfig;

    public List<String> readFile(String path) {
        File file = new File(path);
        FILE_NAME = file.getName();
        ORIGIN_PATH = file.getParent();
        ORIGIN_FULL_PATH = file.getPath();
        NEW_FULL_PATH = String.format("%s\\[TRANS]%s", ORIGIN_PATH, FILE_NAME);
        file = new File(NEW_FULL_PATH);
        file.delete();
        try {
            Stream<String> lines = Files.lines(Paths.get(path));
            List<String> txt = lines.collect(Collectors.toList());
            System.out.println();
            return delMultiEmptyLine(txt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> delMultiEmptyLine(List<String> txt) {
        List<String> txt1 = new ArrayList<>();
        boolean lastEmpty = false;
        for (String s : txt) {
            if (s.isEmpty() && !lastEmpty) {
                txt1.add(s);
                lastEmpty = true;
            } else if (!s.isEmpty()) {
                txt1.add(s);
                lastEmpty = false;
            }
        }
        return txt1;
    }

    public void writeSubtitle2Local(Subtitle subtitle) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(NEW_FULL_PATH)), StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (Line line : subtitle.getTranslatedLine()) {
            bw.write(String.valueOf(line.getIndex()));
            bw.newLine();
            bw.write(line.getTimestamp());
            bw.newLine();
            if (null != line.getTranslation() && !line.getTranslation().isEmpty()) {
                bw.write(line.getTranslation().replaceAll("\\s*|\r|\n|\t", "")
                );
                bw.newLine();
            }
            bw.write(line.getOriginal());
            bw.newLine();
            bw.newLine();
            bw.flush();
        }
        bw.close();
    }
}
