package com.example.localchat.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.localchat.model.ChatMessage;
import com.example.localchat.model.ChatSession;
import com.example.localchat.repository.ChatRepository;
import com.example.localchat.repository.ChatSessionRepository;
import com.example.localchat.service.ChatHistoryService;
import com.example.localchat.service.LlmRouterService;

@RestController
public class ChatController {

    @Autowired
    private LlmRouterService llmRouterService;

    @Autowired
    private ChatHistoryService chatHistoryService;
    
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository; // Inject the new repository

    @GetMapping("/api/chat")
    public String chat(
            @RequestParam String msg, 
            @RequestParam(defaultValue = "default-user") String sessionId) {
        
        // Auto-naming logic: If this is the first message of the session, automatically save a title (optional)
        if (chatRepository.countBySessionId(sessionId) == 0) {
            String autoTitle = msg.length() > 20 ? msg.substring(0, 20) + "..." : msg;
            chatSessionRepository.save(new ChatSession(sessionId, autoTitle));
        }
        
        return llmRouterService.processRequest(sessionId, msg);
    }

    // Modified: Returns a list of objects {sessionId, title} instead of just strings
    @GetMapping("/api/sessions")
    public List<Map<String, String>> getSessions() {
        // 1. Find all IDs that have conversation history
        List<String> activeIds = chatRepository.findAllSessionIds();
        
        List<Map<String, String>> result = new ArrayList<>();
        
        for (String id : activeIds) {
            Map<String, String> map = new HashMap<>();
            map.put("sessionId", id);
            
            // 2. Check if a custom title exists
            Optional<ChatSession> sessionMeta = chatSessionRepository.findById(id);
            if (sessionMeta.isPresent()) {
                map.put("title", sessionMeta.get().getTitle());
            } else {
                // If no title exists, display the first 8 characters of the ID
                map.put("title", "Chat " + id.substring(0, 8));
            }
            result.add(map);
        }
        return result;
    }

    @GetMapping("/api/history")
    public List<ChatMessage> getHistory(@RequestParam String sessionId) {
        return chatHistoryService.getHistory(sessionId);
    }

    // Added: Rename session
    @PostMapping("/api/sessions/rename")
    public void renameSession(@RequestParam String sessionId, @RequestParam String newTitle) {
        chatSessionRepository.save(new ChatSession(sessionId, newTitle));
    }

    // Added: Delete session
    @DeleteMapping("/api/sessions/delete")
    public void deleteSession(@RequestParam String sessionId) {
        // Old approach (might cause errors):
        // chatRepository.deleteBySessionId(sessionId);
        // chatSessionRepository.deleteById(sessionId);

        //  New approach: Handled by Service (Service layer includes @Transactional)
        chatHistoryService.deleteSession(sessionId);
    }
}