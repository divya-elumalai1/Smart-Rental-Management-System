package com.smartrental.service;

import com.smartrental.model.ChatHistory;
import com.smartrental.model.User;
import com.smartrental.repository.ChatHistoryRepository;
import com.smartrental.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Service for AI-powered chatbot interactions.
 * Uses OpenAI GPT to answer user queries about their rental data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final SecurityUtil securityUtil;

    @Value("${openai.api-key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    @Value("${openai.max-tokens:1000}")
    private int maxTokens;

    @Value("${openai.temperature:0.7}")
    private double temperature;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * Process a user message and return AI response.
     */
    public ChatResponse processMessage(String message, UUID sessionId) {
        User currentUser = securityUtil.getCurrentUser();
        UUID conversationId = sessionId != null ? sessionId : UUID.randomUUID();

        // Save user message
        ChatHistory chatRecord = ChatHistory.builder()
                .user(currentUser)
                .message(message)
                .conversationId(conversationId)
                .build();
        chatHistoryRepository.save(chatRecord);

        String aiResponse;
        String modelUsed = model;

        if (openAiApiKey != null && !openAiApiKey.isBlank() && !openAiApiKey.equals("sk-your-openai-api-key-here")) {
            try {
                aiResponse = callOpenAi(message, currentUser);
                modelUsed = model;
            } catch (Exception e) {
                log.error("OpenAI API call failed: {}", e.getMessage());
                aiResponse = generateFallbackResponse(message);
                modelUsed = "fallback";
            }
        } else {
            log.warn("OpenAI API key not configured. Using fallback response.");
            aiResponse = generateFallbackResponse(message);
            modelUsed = "fallback";
        }

        // Update chat record with response
        chatRecord.setResponse(aiResponse);
        chatRecord.setModelUsed(modelUsed);
        chatHistoryRepository.save(chatRecord);

        log.info("Chat message processed for user: {} (session: {})", currentUser.getEmail(), conversationId);

        return new ChatResponse(chatRecord.getId(), message, aiResponse, conversationId);
    }

    /**
     * Get chat history for the current user.
     */
    @Transactional(readOnly = true)
    public List<ChatHistory> getChatHistory() {
        User currentUser = securityUtil.getCurrentUser();
        return chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

    /**
     * Call the OpenAI API.
     */
    private String callOpenAi(String message, User user) throws Exception {
        String systemPrompt = "You are a helpful AI assistant for Sapthagiri Residency Smart Rental Management System. "
                + "The user's name is " + user.getFirstName() + " " + user.getLastName()
                + " and their role is " + user.getRole() + ". "
                + "Answer questions about rent, maintenance, leases, and property management. "
                + "Keep responses concise and helpful.";

        String requestBody = "{"
                + "\"model\":\"" + model + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + escapeJson(systemPrompt) + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(message) + "\"}"
                + "],"
                + "\"max_tokens\":" + maxTokens + ","
                + "\"temperature\":" + temperature
                + "}";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return extractContentFromResponse(response.body());
        } else {
            log.error("OpenAI API returned status: {} body: {}", response.statusCode(), response.body());
            throw new RuntimeException("OpenAI API error: " + response.statusCode());
        }
    }

    /**
     * Generate contextual fallback response when AI is unavailable.
     */
    private String generateFallbackResponse(String message) {
        String lowerMsg = message.toLowerCase();

        if (lowerMsg.contains("rent") || lowerMsg.contains("pay") || lowerMsg.contains("due")) {
            return "You can view and pay your rent in the Payments section of your dashboard. "
                    + "Please check your dashboard for upcoming due dates and amounts.";
        }
        if (lowerMsg.contains("maintenance") || lowerMsg.contains("repair") || lowerMsg.contains("fix")) {
            return "To submit a maintenance request, go to the Maintenance section in your dashboard. "
                    + "Our team will review and address your request promptly.";
        }
        if (lowerMsg.contains("lease") || lowerMsg.contains("agreement") || lowerMsg.contains("contract")) {
            return "Your lease details are available in the Documents section of your dashboard. "
                    + "You can view your lease agreement, start and end dates there.";
        }
        if (lowerMsg.contains("hello") || lowerMsg.contains("hi")) {
            return "Hello! I'm your Sapthagiri Residency AI assistant. How can I help you today? "
                    + "You can ask me about rent, maintenance, leases, or documents.";
        }
        if (lowerMsg.contains("thank")) {
            return "You're welcome! If you have any more questions, feel free to ask. Have a great day!";
        }

        return "I'm your AI assistant for Sapthagiri Residency. I can help with: "
                + "rent payments and due dates, maintenance requests, lease and document information, "
                + "and general property inquiries. Please ask a specific question so I can assist you better.";
    }

    /**
     * Escape a string for safe inclusion in JSON.
     */
    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Extract response content from OpenAI API JSON response.
     */
    private String extractContentFromResponse(String body) {
        try {
            // Find "content":" pattern
            String searchKey = "\"content\":\"";
            int contentIdx = body.indexOf(searchKey);
            if (contentIdx == -1) return "I could not process your request at this time.";
            int start = contentIdx + searchKey.length();
            StringBuilder content = new StringBuilder();
            for (int i = start; i < body.length(); i++) {
                char c = body.charAt(i);
                if (c == '"') {
                    // Check if escaped
                    if (i > 0 && body.charAt(i - 1) == '\\') {
                        content.append(c);
                    } else {
                        break;
                    }
                } else {
                    content.append(c);
                }
            }
            return content.toString()
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\t", "\t")
                    .replace("\\\\", "\\");
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage());
            return "I could not process your request at this time.";
        }
    }

    /**
     * Response DTO for chat messages.
     */
    public record ChatResponse(UUID id, String message, String response, UUID conversationId) {}
}
