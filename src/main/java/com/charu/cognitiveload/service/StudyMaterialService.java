package com.charu.cognitiveload.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class StudyMaterialService {

    // Wikipedia API — completely free
    private static final String WIKI_API =
            "https://en.wikipedia.org/w/api.php";

    public List<Map<String, String>> findStudyMaterial(
            String documentText) {

        List<Map<String, String>> materials = new ArrayList<>();

        try {
            // Extract top keywords from document
            String keywords = extractTopKeywords(documentText);

            // Search Wikipedia
            String params = "?action=opensearch&search=" +
                    URLEncoder.encode(keywords,
                            StandardCharsets.UTF_8) +
                    "&limit=5&format=json";

            URL url = new URL(WIKI_API + params);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent",
                    "CogniLoad/1.0");

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse Wikipedia response
            // Response format: [query, [titles], [descriptions], [urls]]
            JSONArray result =
                    new JSONArray(response.toString());

            JSONArray titles = result.getJSONArray(1);
            JSONArray descriptions = result.getJSONArray(2);
            JSONArray urls = result.getJSONArray(3);

            for (int i = 0; i < Math.min(titles.length(), 5); i++) {
                Map<String, String> material = new HashMap<>();
                material.put("title", titles.getString(i));
                material.put("description",
                        descriptions.getString(i).isEmpty()
                                ? "Wikipedia Article"
                                : descriptions.getString(i));
                material.put("url", urls.getString(i));
                material.put("source", "Wikipedia");
                materials.add(material);
            }

        } catch (Exception e) {
            System.out.println("Study material fetch failed: "
                    + e.getMessage());
        }

        return materials;
    }

    private String extractTopKeywords(String text) {
        // Remove common words
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the","a","an","is","it","in","on","at","to",
                "and","or","of","for","with","this","that",
                "are","was","were","be","been","has","have"
        ));

        Map<String, Integer> freq = new HashMap<>();
        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", "").split("\\s+");

        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 4) {
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }

        // Get top 3 keywords
        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>
                        comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .reduce("", (a, b) -> a + " " + b)
                .trim();
    }
}