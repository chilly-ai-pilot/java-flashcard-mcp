package com.chilly.flashcardmcp.tool;

import com.chilly.flashcardmcp.model.Flashcard;
import com.chilly.flashcardmcp.repository.FlashcardRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlashcardTool {

    private final FlashcardRepository flashcardRepository;

    public FlashcardTool(FlashcardRepository flashcardRepository) {
        this.flashcardRepository = flashcardRepository;
    }

    @Tool(description = "在数据库中持久化创建一张新的抽认卡（flashcard），并返回创建后的完整记录（含自动生成的id和时间戳）。当用户要求保存、记录、新增学习卡片、生词卡、知识点卡片，或说'帮我记一下/存一下xxx'时，应该调用此工具真正写入数据库，而不是只在对话中口头复述内容。")
    public Flashcard createFlashcard(
            @ToolParam(description = "抽认卡标题，例如单词、知识点名称或主题") String title,
            @ToolParam(description = "抽认卡内容，例如释义、解释、详细说明或答案") String content) {
        Flashcard flashcard = new Flashcard();
        flashcard.setTitle(title);
        flashcard.setContent(content);
        return flashcardRepository.save(flashcard);
    }

    @Tool(description = "从数据库中查询并返回所有已保存的抽认卡完整列表。当用户想查看、回顾、复习已有的卡片，或问'我保存了哪些/有多少张卡片'时应调用此工具，而不是凭空回答。")
    public List<Flashcard> listCards() {
        return flashcardRepository.findAll();
    }
}