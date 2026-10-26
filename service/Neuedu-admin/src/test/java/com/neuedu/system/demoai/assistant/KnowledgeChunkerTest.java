package com.neuedu.system.demoai.assistant;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KnowledgeChunkerTest {
    @Test
    public void keepsShortParagraphsTogetherAndIgnoresBlankInput() {
        List<String> chunks = KnowledgeChunker.split("配送说明\n\n售后说明");
        assertEquals(1, chunks.size());
        assertTrue(chunks.get(0).contains("配送说明"));
        assertTrue(KnowledgeChunker.split(" \n  ").isEmpty());
    }

    @Test
    public void boundsLongChunksAndCarriesOverlapAcrossLongParagraphs() {
        String text = repeat('甲', 2800);
        List<String> chunks = KnowledgeChunker.split(text);
        assertTrue(chunks.size() >= 3);
        for (String chunk : chunks) assertTrue(chunk.length() <= 1200);
        assertTrue(chunks.get(0).substring(chunks.get(0).length() - 120)
                .equals(chunks.get(1).substring(0, 120)));
    }

    private String repeat(char value, int count) {
        StringBuilder result = new StringBuilder(count);
        for (int i = 0; i < count; i++) result.append(value);
        return result.toString();
    }
}
