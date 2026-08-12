package com.chilly.flashcardmcp.config;

import com.chilly.flashcardmcp.tool.FlashcardTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlashcardToolConfiguration {

    @Bean
    public ToolCallbackProvider flashcardToolCallbackProvider(FlashcardTool flashcardTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(flashcardTool)
                .build();
    }
}
