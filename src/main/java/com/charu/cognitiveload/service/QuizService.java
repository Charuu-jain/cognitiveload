package com.charu.cognitiveload.service;

import com.charu.cognitiveload.model.QuizQuestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class QuizService {

    @Autowired
    private GeminiService geminiService;

    public List<QuizQuestion> generateQuiz(
            String text, Long documentId) {

        List<QuizQuestion> questions = new ArrayList<>();

        // Try Gemini first
        try {
            String jsonResponse =
                geminiService.generateQuizJSON(text);

            if (jsonResponse != null) {
                // Clean response — remove markdown
                String cleaned = jsonResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

                JSONArray arr = new JSONArray(cleaned);

                for (int i = 0;
                        i < Math.min(arr.length(), 5);
                        i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    QuizQuestion q = new QuizQuestion();
                    q.setDocumentId(documentId);
                    q.setQuestion(
                        obj.getString("question"));
                    q.setOptionA(
                        obj.getString("optionA"));
                    q.setOptionB(
                        obj.getString("optionB"));
                    q.setOptionC(
                        obj.getString("optionC"));
                    q.setOptionD(
                        obj.getString("optionD"));
                    q.setCorrectAnswer(
                        obj.getString("correct"));
                    questions.add(q);
                }

                if (!questions.isEmpty())
                    return questions;
            }
        } catch (Exception e) {
            System.out.println("Gemini quiz failed," +
                " using rule-based: " + e.getMessage());
        }

        // Fallback to rule-based
        return generateRuleBasedQuiz(text, documentId);
    }

    private List<QuizQuestion> generateRuleBasedQuiz(
            String text, Long documentId) {
        List<QuizQuestion> questions = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        int count = 0;

        for (String sentence : sentences) {
            if (count >= 5) break;
            sentence = sentence.trim();
            String[] words = sentence.split("\\s+");
            if (words.length < 10 ||
                    words.length > 35) continue;
            if (sentence.length() < 60) continue;

            String keyword = findKeyword(sentence);
            if (keyword == null) continue;

            String questionText =
                sentence.replace(keyword, "_____");

            QuizQuestion q = new QuizQuestion();
            q.setDocumentId(documentId);
            q.setQuestion("Fill in the blank: "
                + questionText);
            q.setOptionA(keyword);
            q.setOptionB(generateWrong(keyword, 1));
            q.setOptionC(generateWrong(keyword, 2));
            q.setOptionD(generateWrong(keyword, 3));
            q.setCorrectAnswer("A");
            questions.add(q);
            count++;
        }
        return questions;
    }

    private String findKeyword(String sentence) {
        for (String word : sentence.split("\\s+")) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (word.length() >= 6 &&
                    !isStopWord(word)) return word;
        }
        return null;
    }

    private String generateWrong(
            String correct, int variant) {
        String[] prefixes = {"Un","Re","Pre","In"};
        String[] suffixes = {"tion","ment","ity","ness"};
        switch (variant) {
            case 1: return prefixes[
                correct.length() % 4] +
                correct.toLowerCase();
            case 2: return correct.substring(0,
                Math.min(correct.length(), 4)) +
                suffixes[correct.length() % 4];
            default: return correct.charAt(0) +
                correct.substring(1).toLowerCase() +
                "ing";
        }
    }

    private boolean isStopWord(String word) {
        Set<String> stops = new HashSet<>(Arrays.asList(
            "this","that","with","from","they",
            "their","there","which","would","could"
        ));
        return stops.contains(word.toLowerCase());
    }
}