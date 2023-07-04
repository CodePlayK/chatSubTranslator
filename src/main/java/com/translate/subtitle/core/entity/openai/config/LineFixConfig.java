package com.translate.subtitle.core.entity.openai.config;

import com.translate.subtitle.core.entity.openai.LineFix;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Data
@ConfigurationProperties(prefix = "openai-service.fixer")
public class LineFixConfig {
    private String fixFileName;
    private List<LineFix> lineFixs;
}
