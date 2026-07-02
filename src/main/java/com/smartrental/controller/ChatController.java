package com.smartrental.controller;

import com.smartrental.model.ChatHistory;
import com.smartrental.service.ChatService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for AI chatbot interactions.
 */
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * Send a message to the AI assistant.
     * POST /api/v1/chat/message
     */
    @PostMapping("/message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatService.ChatResponse> sendMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        String sessionIdStr = request.get("sessionId");
        UUID sessionId = (sessionIdStr != null && !sessionIdStr.isBlank())
                ? UUID.fromString(sessionIdStr)
                : null;

        log.info("POST chat message (session: {})", sessionId);
        ChatService.ChatResponse response = chatService.processMessage(message, sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get chat history for the current user.
     * GET /api/v1/chat/history
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatHistory>> getChatHistory() {
        log.debug("GET chat history");
        return ResponseEntity.ok(chatService.getChatHistory());
    }
}
