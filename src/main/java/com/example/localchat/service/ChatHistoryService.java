package com.example.localchat.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.localchat.model.ChatMessage;
import com.example.localchat.repository.ChatRepository;
import com.example.localchat.repository.ChatSessionRepository; // 1. 引入 Session Repository

@Service
public class ChatHistoryService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository; // 2. 注入它

    private static final int MAX_HISTORY = 20;

    public List<ChatMessage> getHistory(String sessionId) {
        return chatRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public void addMessage(String sessionId, String role, String content) {
        ChatMessage message = new ChatMessage(sessionId, role, content);
        chatRepository.save(message);

        long count = chatRepository.countBySessionId(sessionId);
        if (count > MAX_HISTORY) {
            ChatMessage oldest = chatRepository.findFirstBySessionIdOrderByCreatedAtAsc(sessionId);
            if (oldest != null) {
                chatRepository.delete(oldest);
            }
        }
    }

    // 3. ★ 修改這裡：完整刪除方法 (包含訊息和標題) ★
    @Transactional // 這個註解非常重要！沒有它，delete 會失敗
    public void deleteSession(String sessionId) {
        // 先刪除所有對話紀錄
        chatRepository.deleteBySessionId(sessionId);
        // 再刪除 Session 標題
        chatSessionRepository.deleteById(sessionId);
    }
    
    // 舊的 clearHistory 可以保留或刪除，建議用上面的 deleteSession 取代
}