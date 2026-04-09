package com.example.cinema.service.ai.impl;

import com.example.cinema.service.ai.GeminiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.endpoint}")
    private String apiUrl;

    private String getFullUrl() {
        return apiUrl + "?key=" + apiKey;
    }

    @Override
    public String generateResponse(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        
        // Prepare request body for Gemini API
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", Collections.singletonList(part));
        requestBody.put("contents", Collections.singletonList(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(getFullUrl(), entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                // Extract text from Gemini response structure
                List<Map> candidates = (List<Map>) response.getBody().get("candidates");
                Map contentRes = (Map) candidates.get(0).get("content");
                List<Map> parts = (List<Map>) contentRes.get("parts");
                return (String) parts.get(0).get("text");
            }
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            throw new RuntimeException("Gemini API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Gemini API Error: " + e.getMessage());
        }
        
        return "[]"; 
    }
}
