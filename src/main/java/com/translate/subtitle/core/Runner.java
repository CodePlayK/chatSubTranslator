package com.translate.subtitle.core;

import com.translate.subtitle.core.service.FixService;
import com.translate.subtitle.core.service.SubtitleService;
import com.translate.subtitle.core.service.WhisperService;
import com.translate.subtitle.core.util.ChatGPTRequestUtil;
import com.translate.subtitle.core.util.webUtils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {

    @Autowired
    private SubtitleService subtitleService;
    @Autowired
    private WhisperService whisperService;
    @Autowired
    private FixService fixService;
    @Autowired
    private ChatGPTRequestUtil chatGPTRequestUtil;
    @Autowired
    private CookieUtils cookieUtils;

    @Override
    public void run(String... args) throws Exception {
        preRun();
        //chatGPT翻译
        subtitleService.translate();
        ////Whisper音频转字幕
        //whisperService.whisper();
        ////修复字幕
        //fixService.fix();
    }

    private void preRun() {
        cookieUtils.getSysProxy();
    }

}
