package com.charu.cognitiveload.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GrammarCheckService {

    // Uses LanguageTool public API — completely free
    private static final String API_URL =
            "https://api.languagetool.org/v2/check";

    public List<Map<String, String>> checkGrammar(String text) {
        List<Map<String, String>> errors = new ArrayList<>();

        try {
            // Limit text to 1000 chars for free API
            String checkText = text.length() > 1000
                    ? text.substring(0, 1000) : text;

            // Build POST request
            String params = "text=" +
                    URLEncoder.encode(checkText, StandardCharsets.UTF_8) +
                    "&language=en-US";

            URL url = new URL(API_URL);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            // Read response
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse JSON response
            JSONObject json = new JSONObject(response.toString());
            JSONArray matches = json.getJSONArray("matches");

            for (int i = 0; i < Math.min(matches.length(), 8); i++) {
                JSONObject match = matches.getJSONObject(i);
                Map<String, String> error = new HashMap<>();

                error.put("message",
                        match.getString("message"));
                error.put("context",
                        match.getJSONObject("context")
                                .getString("text"));

                // Get first suggestion if available
                JSONArray replacements =
                        match.getJSONArray("replacements");
                if (replacements.length() > 0) {
                    error.put("suggestion",
                            replacements.getJSONObject(0)
                                    .getString("value"));
                } else {
                    error.put("suggestion", "No suggestion");
                }

                errors.add(error);
            }

        } catch (Exception e) {
            // If API fails return empty list
            System.out.println("Grammar check failed: "
                    + e.getMessage());
        }

        return errors;
    }
}