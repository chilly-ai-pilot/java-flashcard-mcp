package com.chilly.flashcardmcp.repository;

import com.chilly.flashcardmcp.model.Flashcard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FlashcardRepositoryTest {

    @Autowired
    private FlashcardRepository flashcardRepository;

    @Test
    void shouldSaveAndListFlashcards() {
        Flashcard flashcard = new Flashcard();
        flashcard.setTitle("Java MCP");
        flashcard.setContent("Spring AI MCP Server + JPA");

        Flashcard saved = flashcardRepository.save(flashcard);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        List<Flashcard> cards = flashcardRepository.findAll();
        assertThat(cards).hasSize(1);
        assertThat(cards.get(0).getTitle()).isEqualTo("Java MCP");
    }
}
