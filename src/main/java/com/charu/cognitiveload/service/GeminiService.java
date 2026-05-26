package com.charu.cognitiveload.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    // Core method to call Gemini API
    public String generate(String prompt) {
        try {
            URL url = new URL(API_URL + apiKey);
            HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty(
                "Content-Type", "application/json");

            // Build request body
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contents);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString()
                    .getBytes(StandardCharsets.UTF_8));
            }

            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                        conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse response
            JSONObject json =
                new JSONObject(response.toString());
            return json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

        } catch (Exception e) {
            System.out.println("Gemini API error: "
                + e.getMessage());
            return null;
        }
    }

    // SUMMARIZATION
    public String summarize(String text) {
        // Limit text to avoid token limits
        String trimmed = text.length() > 3000
            ? text.substring(0, 3000) : text;

        String prompt =
            "You are an academic content analyzer. " +
            "Summarize the following academic document " +
            "in exactly 5 clear bullet points. " +
            "Each bullet point should be one complete " +
            "sentence explaining a key concept. " +
            "Format: start each point with '• '. " +
            "Do not add any intro or outro text.\n\n"
            + trimmed;

        String result = generate(prompt);
        return result != null ? result
            : "Summary could not be generated.";
    }

    // QUIZ GENERATION
    public String generateQuizJSON(String text) {
        String trimmed = text.length() > 2000
            ? text.substring(0, 2000) : text;

        String prompt =
            "You are a quiz generator. Based on this " +
            "academic text, generate exactly 5 multiple " +
            "choice questions. " +
            "Return ONLY a JSON array, no other text. " +
            "Format:\n" +
            "[{\"question\":\"...\",\"optionA\":\"...\"," +
            "\"optionB\":\"...\",\"optionC\":\"...\"," +
            "\"optionD\":\"...\",\"correct\":\"A\"}]\n\n"
            + trimmed;

        return generate(prompt);
    }

    // FLASHCARD GENERATION
    public String generateFlashcardsJSON(String text) {
        String trimmed = text.length() > 2000
            ? text.substring(0, 2000) : text;

        String prompt =
            "You are a flashcard generator. Based on " +
            "this academic text, generate exactly 8 " +
            "flashcards for key terms and concepts. " +
            "Return ONLY a JSON array, no other text. " +
            "Format:\n" +
            "[{\"term\":\"...\",\"definition\":\"...\"}]\n\n"
            + trimmed;

        return generate(prompt);
    }
}