package com.example.localchat.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.localchat.model.ChatMessage;
import com.example.localchat.repository.ChatRepository;
import com.example.localchat.repository.ChatSessionRepository; // 1. Import Session Repository

@Service
public class ChatHistoryService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository; // 2. Inject the repository

    private static final int MAX_HISTORY = 20;

    public List<ChatMessage> getHistory(String sessionId) {
        return chatRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public void addMessage(String sessionId, String role, String content) {
        ChatMessage message = new ChatMessage(sessionId, role, content);
        chatRepository.save(message);

        // Sliding window logic: maintain maximum message history
        long count = chatRepository.countBySessionId(sessionId);
        if (count > MAX_HISTORY) {
            ChatMessage oldest = chatRepository.findFirstBySessionIdOrderByCreatedAtAsc(sessionId);
            if (oldest != null) {
                chatRepository.delete(oldest);
            }
        }
    }

    // 3. Updated: Full deletion method (includes both messages and session titles)
    @Transactional // This annotation is crucial! Deletion operations will fail without it.
    public void deleteSession(String sessionId) {
        // First, delete all conversation records
        chatRepository.deleteBySessionId(sessionId);
        
        // Then, delete the Session title metadata
        chatSessionRepository.deleteById(sessionId);
    }
    
    // The old clearHistory method can be kept or removed; 
    // it is recommended to use the deleteSession method above instead.
}