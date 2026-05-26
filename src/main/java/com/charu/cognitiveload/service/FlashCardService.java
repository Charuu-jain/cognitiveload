package com.charu.cognitiveload.service;

import com.charu.cognitiveload.model.FlashCard;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FlashCardService {

    @Autowired
    private GeminiService geminiService;

    public List<FlashCard> generateFlashcards(
            String text, Long documentId) {

        List<FlashCard> flashcards = new ArrayList<>();

        // Try Gemini first
        try {
            String jsonResponse =
                geminiService.generateFlashcardsJSON(text);

            if (jsonResponse != null) {
                String cleaned = jsonResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

                JSONArray arr = new JSONArray(cleaned);

                for (int i = 0;
                        i < Math.min(arr.length(), 8);
                        i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    FlashCard fc = new FlashCard();
                    fc.setDocumentId(documentId);
                    fc.setTerm(obj.getString("term"));
                    fc.setDefinition(
                        obj.getString("definition"));
                    flashcards.add(fc);
                }

                if (!flashcards.isEmpty())
                    return flashcards;
            }
        } catch (Exception e) {
            System.out.println("Gemini flashcards failed," +
                " using rule-based: " + e.getMessage());
        }

        // Fallback to rule-based
        return generateRuleBasedFlashcards(
            text, documentId);
    }

    private List<FlashCard> generateRuleBasedFlashcards(
            String text, Long documentId) {
        List<FlashCard> flashcards = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        Set<String> usedTerms = new HashSet<>();

        for (String sentence : sentences) {
            if (flashcards.size() >= 8) break;
            sentence = sentence.trim();
            if (sentence.length() < 50) continue;

            String term = null;
            String definition = null;

            if (sentence.contains(" is ")) {
                String[] parts =
                    sentence.split(" is ", 2);
                term = cleanTerm(parts[0]);
                definition = parts[1].trim();
            } else if (sentence.contains(" means ")) {
                String[] parts =
                    sentence.split(" means ", 2);
                term = cleanTerm(parts[0]);
                definition = parts[1].trim();
            }

            if (term != null && definition != null
                    && term.length() > 2
                    && term.length() < 60
                    && !usedTerms.contains(
                        term.toLowerCase())) {
                FlashCard fc = new FlashCard();
                fc.setDocumentId(documentId);
                fc.setTerm(capitalize(term));
                fc.setDefinition(definition);
                flashcards.add(fc);
                usedTerms.add(term.toLowerCase());
            }
        }
        return flashcards;
    }

    private String cleanTerm(String raw) {
        String[] words = raw.trim().split("\\s+");
        int start = Math.max(0, words.length - 3);
        StringBuilder term = new StringBuilder();
        for (int i = start; i < words.length; i++) {
            if (i > start) term.append(" ");
            term.append(words[i].replaceAll(
                "[^a-zA-Z\\s]", ""));
        }
        return term.toString().trim();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0))
            + s.substring(1);
    }
}