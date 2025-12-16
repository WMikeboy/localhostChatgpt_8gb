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
    private ChatSessionRepository chatSessionRepository; // 注入新倉庫

    @GetMapping("/api/chat")
    public String chat(
            @RequestParam String msg, 
            @RequestParam(defaultValue = "default-user") String sessionId) {
        
        // 自動命名邏輯：如果是該 Session 的第一句話，自動幫它存一個標題 (選用)
        if (chatRepository.countBySessionId(sessionId) == 0) {
            String autoTitle = msg.length() > 20 ? msg.substring(0, 20) + "..." : msg;
            chatSessionRepository.save(new ChatSession(sessionId, autoTitle));
        }
        
        return llmRouterService.processRequest(sessionId, msg);
    }

    // ★ 修改：回傳物件列表 {sessionId, title} 而不是只有字串
    @GetMapping("/api/sessions")
    public List<Map<String, String>> getSessions() {
        // 1. 找出所有有對話紀錄的 ID
        List<String> activeIds = chatRepository.findAllSessionIds();
        
        List<Map<String, String>> result = new ArrayList<>();
        
        for (String id : activeIds) {
            Map<String, String> map = new HashMap<>();
            map.put("sessionId", id);
            
            // 2. 去查查看有沒有自訂標題
            Optional<ChatSession> sessionMeta = chatSessionRepository.findById(id);
            if (sessionMeta.isPresent()) {
                map.put("title", sessionMeta.get().getTitle());
            } else {
                // 沒有標題就顯示 ID 前 8 碼
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

    // ★ 新增：重新命名
    @PostMapping("/api/sessions/rename")
    public void renameSession(@RequestParam String sessionId, @RequestParam String newTitle) {
        chatSessionRepository.save(new ChatSession(sessionId, newTitle));
    }

    // ★ 新增：刪除對話
    @DeleteMapping("/api/sessions/delete")
    public void deleteSession(@RequestParam String sessionId) {
        // 舊的寫法 (會報錯)：
        // chatRepository.deleteBySessionId(sessionId);
        // chatSessionRepository.deleteById(sessionId);

        // ✅ 新的寫法：交給 Service 處理 (Service 有 @Transactional)
        chatHistoryService.deleteSession(sessionId);
    }
}