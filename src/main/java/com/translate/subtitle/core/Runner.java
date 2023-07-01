package com.translate.subtitle.core;

import com.translate.subtitle.core.service.SubtitleService;
import com.translate.subtitle.core.util.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {
    @Autowired
    private SubtitleService subtitleService;
    @Autowired
    private CookieUtils cookieUtils;

    @Override
    public void run(String... args) throws Exception {
        preRun();
        subtitleService.process();
        System.out.println();
    }

    public void preRun() {
        cookieUtils.getSysProxy();
    }
}
