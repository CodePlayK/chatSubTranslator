package com.translate.subtitle.core.service;

import com.translate.subtitle.core.entity.openai.config.OpenAiConfig;
import com.translate.subtitle.core.util.WhisperRequestUtil;
import com.translate.subtitle.core.util.localUtils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class WhisperService {

    @Autowired
    private WhisperRequestUtil whisperRequestUtil;
    @Autowired
    private OpenAiConfig openAiConfig;
    @Autowired
    private FileUtil fileUtil;

    public void whisper() {
        try {
            ResponseEntity<String> whisper = whisperRequestUtil.whisper(new File(openAiConfig.getAudioFileName()));
            String body = whisper.getBody();
            File file = new File(openAiConfig.getAudioFileName());
            String fileParent = file.getParent();
            String fileName = StringUtils.substringBefore(file.getName(), ".");
            String path = String.format("%s\\[ChatGPT-SUB]%s.srt", fileParent, fileName);
            fileUtil.writeString2Local(body, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
