package com.charu.cognitiveload.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SummarizationService {

    public String summarize(String text, int topN) {

        // Step 1: Clean the text
        String cleaned = text
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n");

        // Step 2: Split into sentences
        String[] sentences = cleaned.split("(?<=[.!?])\\s+");

        if (sentences.length <= topN) {
            return cleaned.substring(0, Math.min(cleaned.length(), 800));
        }

        // Step 3: Filter out bad sentences
        List<String> goodSentences = new ArrayList<>();
        for (int i = 0; i < sentences.length; i++) {
            String s = sentences[i].trim();

            // Skip if too short
            if (s.length() < 60) continue;

            // Skip if too few words
            int wordCount = s.split("\\s+").length;
            if (wordCount < 10) continue;

            // Skip if looks like a heading (all caps or very short line)
            if (s.equals(s.toUpperCase())) continue;

            // Skip if it's just numbers or bullet points
            if (s.matches("^[\\d\\.\\-\\•\\*].*") && wordCount < 8) continue;

            // Skip if it contains too many special characters
            if (s.matches(".*[|=]{3,}.*")) continue;

            goodSentences.add(s);
        }

        if (goodSentences.isEmpty()) {
            return "Summary could not be generated for this document.";
        }

        // Step 4: Score each good sentence
        Map<String, Integer> wordFreq = getWordFrequency(text);
        Map<String, Double> scores = new LinkedHashMap<>();

        for (int i = 0; i < goodSentences.size(); i++) {
            String sentence = goodSentences.get(i);
            double score = 0;

            // Keyword score — most important factor
            score += keywordScore(sentence, wordFreq) * 3;

            // Position score
            score += positionScore(i, goodSentences.size());

            // Prefer sentences with verbs/explanations
            // Sentences with "is", "are", "means", "refers", "used" are explanatory
            String lower = sentence.toLowerCase();
            if (lower.contains(" is ") || lower.contains(" are ") ||
                    lower.contains(" means ") || lower.contains(" refers ") ||
                    lower.contains(" used ") || lower.contains(" allows ") ||
                    lower.contains(" provides ") || lower.contains(" helps ")) {
                score += 1.5;
            }

            // Prefer medium length sentences
            score += lengthScore(sentence);

            scores.put(sentence, score);
        }

        // Step 5: Pick top N sentences
        List<String> top = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return String.join(" • ", top);
    }

    private Map<String, Integer> getWordFrequency(String text) {
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the","a","an","is","it","in","on","at","to","and","or",
                "but","of","for","with","this","that","are","was","were",
                "be","been","has","have","had","by","from","as","its",
                "which","they","their","there","then","than","when","who",
                "what","how","will","can","use","used","using","also","into",
                "more","such","each","after","before","between","about","over"
        ));

        Map<String, Integer> freq = new HashMap<>();
        for (String word : text.toLowerCase().split("\\s+")) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (!word.isEmpty() && !stopWords.contains(word) && word.length() > 3) {
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }
        return freq;
    }

    private double keywordScore(String sentence, Map<String, Integer> freq) {
        String[] words = sentence.toLowerCase().split("\\s+");
        double score = 0;
        for (String w : words) {
            w = w.replaceAll("[^a-zA-Z]", "");
            score += freq.getOrDefault(w, 0);
        }
        return score / Math.max(words.length, 1);
    }

    private double positionScore(int index, int total) {
        double pos = (double) index / total;
        if (pos <= 0.15) return 2.0;
        if (pos >= 0.85) return 1.5;
        if (pos <= 0.30) return 1.0;
        return 0.5;
    }

    private double lengthScore(String sentence) {
        int wc = sentence.split("\\s+").length;
        if (wc >= 12 && wc <= 30) return 2.0;
        if (wc >= 8  && wc <= 40) return 1.0;
        return 0.2;
    }
}