package com.neuedu.system.demoai.assistant;

import java.util.ArrayList;
import java.util.List;

/** Splits administrator-authored knowledge into bounded, overlap-aware retrieval passages. */
public final class KnowledgeChunker {
    private static final int MAX_CHARS = 1200;
    private static final int OVERLAP_CHARS = 120;

    private KnowledgeChunker() { }

    public static List<String> split(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) return chunks;
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n').trim();
        List<String> paragraphs = new ArrayList<>();
        for (String paragraph : normalized.split("\\n\\s*\\n+")) {
            String value = paragraph.trim();
            if (!value.isEmpty()) paragraphs.add(value);
        }
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (paragraph.length() > MAX_CHARS) {
                flush(current, chunks);
                appendLongParagraph(paragraph, chunks);
                continue;
            }
            if (current.length() > 0 && current.length() + paragraph.length() + 2 > MAX_CHARS) {
                String previous = current.toString();
                chunks.add(previous);
                current.setLength(0);
                if (paragraph.length() + OVERLAP_CHARS + 2 <= MAX_CHARS) {
                    String overlap = previous.substring(Math.max(0, previous.length() - OVERLAP_CHARS));
                    current.append(overlap);
                }
            }
            if (current.length() > 0) current.append("\n\n");
            current.append(paragraph);
        }
        flush(current, chunks);
        return chunks;
    }

    private static void appendLongParagraph(String paragraph, List<String> chunks) {
        int start = 0;
        while (start < paragraph.length()) {
            int end = Math.min(start + MAX_CHARS, paragraph.length());
            if (end < paragraph.length()) {
                int boundary = paragraph.lastIndexOf('。', end - 1);
                if (boundary > start + MAX_CHARS / 2) end = boundary + 1;
            }
            chunks.add(paragraph.substring(start, end).trim());
            if (end >= paragraph.length()) break;
            start = Math.max(start + 1, end - OVERLAP_CHARS);
        }
    }

    private static void flush(StringBuilder current, List<String> chunks) {
        String value = current.toString().trim();
        if (!value.isEmpty()) chunks.add(value);
        current.setLength(0);
    }
}
