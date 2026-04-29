package com.charu.cognitiveload.service;

import com.charu.cognitiveload.model.QuizQuestion;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class QuizService {

    public List<QuizQuestion> generateQuiz(
            String text, Long documentId) {

        List<QuizQuestion> questions = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");

        int count = 0;
        for (String sentence : sentences) {
            if (count >= 5) break;
            sentence = sentence.trim();

            // Only use good sentences
            String[] words = sentence.split("\\s+");
            if (words.length < 10 || words.length > 35) continue;
            if (sentence.length() < 60) continue;

            // Find a keyword to blank out
            String keyword = findKeyword(sentence);
            if (keyword == null) continue;

            // Create fill-in-the-blank question
            String questionText = sentence.replace(
                    keyword, "_____");

            QuizQuestion q = new QuizQuestion();
            q.setDocumentId(documentId);
            q.setQuestion("Fill in the blank: "
                    + questionText);
            q.setOptionA(keyword);
            q.setOptionB(generateWrongAnswer(keyword, 1));
            q.setOptionC(generateWrongAnswer(keyword, 2));
            q.setOptionD(generateWrongAnswer(keyword, 3));
            q.setCorrectAnswer("A");

            questions.add(q);
            count++;
        }

        return questions;
    }

    private String findKeyword(String sentence) {
        // Look for technical or important words
        String[] words = sentence.split("\\s+");
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z]", "");
            // Good keyword: 6+ chars, not a stop word
            if (word.length() >= 6 && !isStopWord(word)) {
                return word;
            }
        }
        return null;
    }

    private String generateWrongAnswer(
            String correct, int variant) {
        // Generate plausible wrong answers
        String[] prefixes = {"Un", "Re", "Pre", "In"};
        String[] suffixes = {"tion", "ment", "ity", "ness"};

        switch (variant) {
            case 1:
                return prefixes[correct.length() % 4]
                        + correct.toLowerCase();
            case 2:
                return correct.substring(0,
                        Math.min(correct.length(), 4))
                        + suffixes[correct.length() % 4];
            case 3:
                return correct.charAt(0) +
                        correct.substring(1).toLowerCase()
                        + "ing";
            default:
                return "None of the above";
        }
    }

    private boolean isStopWord(String word) {
        Set<String> stops = new HashSet<>(Arrays.asList(
                "this","that","with","from","they",
                "their","there","which","would","could",
                "should","about","after","before"
        ));
        return stops.contains(word.toLowerCase());
    }
}