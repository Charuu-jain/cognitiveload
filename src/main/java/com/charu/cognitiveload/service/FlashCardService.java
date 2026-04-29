package com.charu.cognitiveload.service;

import com.charu.cognitiveload.model.FlashCard;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FlashCardService {

    private static final Set<String> DOMAIN_TERMS =
            new HashSet<>(Arrays.asList(
                    "algorithm", "function", "variable", "class",
                    "object", "method", "interface", "database",
                    "server", "client", "protocol", "network",
                    "process", "thread", "memory", "cache",
                    "servlet", "request", "response", "session",
                    "authentication", "authorization", "encryption",
                    "abstraction", "inheritance", "polymorphism",
                    "hypothesis", "theorem", "equation", "formula",
                    "synthesis", "analysis", "metabolism", "catalyst",
                    "derivative", "integral", "vector", "matrix"
            ));

    public List<FlashCard> generateFlashcards(
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
                String[] parts = sentence.split(" is ", 2);
                term = cleanTerm(parts[0]);
                definition = parts[1].trim();
            } else if (sentence.contains(" refers to ")) {
                String[] parts = sentence.split(" refers to ", 2);
                term = cleanTerm(parts[0]);
                definition = "refers to " + parts[1].trim();
            } else if (sentence.contains(" means ")) {
                String[] parts = sentence.split(" means ", 2);
                term = cleanTerm(parts[0]);
                definition = parts[1].trim();
            } else {
                String found = findDomainTerm(sentence);
                if (found != null && !usedTerms.contains(found)) {
                    term = found;
                    definition = sentence.trim();
                }
            }

            if (term != null && definition != null
                    && term.length() > 2
                    && term.length() < 60
                    && !usedTerms.contains(term.toLowerCase())) {

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

    private String findDomainTerm(String sentence) {
        String[] words = sentence.split("\\s+");
        for (String word : words) {
            String clean = word.replaceAll(
                    "[^a-zA-Z]", "").toLowerCase();
            if (DOMAIN_TERMS.contains(clean)) return clean;
        }
        return null;
    }

    private String cleanTerm(String raw) {
        String[] words = raw.trim().split("\\s+");
        int start = Math.max(0, words.length - 3);
        StringBuilder term = new StringBuilder();
        for (int i = start; i < words.length; i++) {
            if (i > start) term.append(" ");
            term.append(words[i].replaceAll("[^a-zA-Z\\s]", ""));
        }
        return term.toString().trim();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0))
                + s.substring(1);
    }
}