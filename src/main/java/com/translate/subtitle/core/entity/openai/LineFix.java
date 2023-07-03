package com.translate.subtitle.core.entity.openai;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class LineFix {
    private int dislocationIndexOpen;
    private int dislocationIndexClose;
    private int moveCount;
}
