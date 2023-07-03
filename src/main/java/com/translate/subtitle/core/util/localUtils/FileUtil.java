package com.translate.subtitle.core.util.localUtils;

import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileUtil {
    private static String NEW_FULL_PATH;


    public List<String> readFile(String path, String newFullPath) {
        NEW_FULL_PATH = newFullPath;
        File file = new File(newFullPath);
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
        if (null != subtitle.getErrorIndex()) {
            bw.write("异常index:" + subtitle.getErrorIndex());
            bw.newLine();
        }
        for (Line line : subtitle.getNewLines()) {
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

    public void writeString2Local(String subtitle, String filePath) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(subtitle);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
