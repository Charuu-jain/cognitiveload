package com.charu.cognitiveload.service;

import com.charu.cognitiveload.model.ParagraphScore;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CLIService {

    // Technical words that increase cognitive load
    private static final Set<String> TECHNICAL_WORDS = new HashSet<>(Arrays.asList(
            "algorithm", "hypothesis", "synthesis", "photosynthesis", "mitochondria",
            "eigenvalue", "derivative", "integral", "paradigm", "entropy", "neural",
            "polymer", "catalysis", "metabolism", "chromosome", "isotope", "quantum",
            "thermodynamics", "electromagnetic", "microprocessor", "recursion", "abstraction",
            "encapsulation", "polymorphism", "inheritance", "synchronization", "asynchronous",
            "correlation", "regression", "probability", "differential", "coefficient"
    ));

    public List<ParagraphScore> analyze(String fullText, Long documentId) {
        // Split by multiple newlines OR by sentences that are long enough
        String[] paragraphs = fullText.split("\n\n+|\r\n\r\n+");

        // If splitting gave too few results, try single newline
        if (paragraphs.length <= 2) {
            paragraphs = fullText.split("\n|\r\n");
        }

        List<ParagraphScore> scores = new ArrayList<>();

        for (String para : paragraphs) {
            para = para.trim();
            // Skip very short lines like headers or page numbers
            if (para.length() < 50) continue;

            double score = calculateCLI(para);
            String level = getLevel(score);

            ParagraphScore ps = new ParagraphScore();
            ps.setDocumentId(documentId);
            ps.setParagraphText(para);
            ps.setCliScore(score);
            ps.setDifficultyLevel(level);
            scores.add(ps);
        }

        return scores;
    }

    private double calculateCLI(String paragraph) {
        String[] words = paragraph.split("\\s+");
        String[] sentences = paragraph.split("[.!?]+");

        // Metric 1: Average sentence length (longer = harder)
        double avgSentenceLength = (double) words.length / Math.max(sentences.length, 1);
        double sentenceScore = Math.min((avgSentenceLength / 30.0) * 40, 40);

        // Metric 2: Technical word density
        long techCount = Arrays.stream(words)
                .map(w -> w.toLowerCase().replaceAll("[^a-z]", ""))
                .filter(TECHNICAL_WORDS::contains)
                .count();
        double techScore = Math.min((techCount / (double) words.length) * 200, 30);

        // Metric 3: Long word ratio (words > 8 characters)
        long longWords = Arrays.stream(words)
                .filter(w -> w.length() > 8)
                .count();
        double longWordScore = Math.min((longWords / (double) words.length) * 60, 20);

        // Final CLI score (0-100)
        double total = sentenceScore + techScore + longWordScore;
        return Math.round(total * 10.0) / 10.0;
    }

    private String getLevel(double score) {
        if (score >= 70) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }
}