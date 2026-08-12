package com.chilly.flashcardmcp.tool;

import com.chilly.flashcardmcp.model.Flashcard;
import com.chilly.flashcardmcp.repository.FlashcardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FlashcardToolTest {

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Test
    void shouldCreateAndListCards() {
        FlashcardTool flashcardTool = new FlashcardTool(flashcardRepository);

        Flashcard created = flashcardTool.createFlashcard("Java MCP", "Spring AI MCP Server + JPA");

        assertThat(created.getId()).isNotNull();
        List<Flashcard> cards = flashcardTool.listCards();
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getTitle()).isEqualTo("Java MCP");
    }
}
