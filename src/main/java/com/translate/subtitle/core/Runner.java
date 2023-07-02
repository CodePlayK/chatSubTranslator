package com.translate.subtitle.core;

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
    private ChatGPTRequestUtil chatGPTRequestUtil;
    @Autowired
    private CookieUtils cookieUtils;

    @Override
    public void run(String... args) throws Exception {
        preRun();
        //whisperService.whisper();
        subtitleService.process();
        System.out.println();
    }

    private void preRun() {
        cookieUtils.getSysProxy();
    }

}
