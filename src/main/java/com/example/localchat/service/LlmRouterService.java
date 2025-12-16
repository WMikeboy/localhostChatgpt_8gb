package com.example.localchat.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.localchat.model.ChatMessage;

@Service
public class LlmRouterService {

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private ComfyUIService comfyUIService;
    
    @Autowired
    private ChatHistoryService chatHistoryService;

    public String processRequest(String sessionId, String userMessage) {
        
        // 1. 先把使用者的話存入歷史
        chatHistoryService.addMessage(sessionId, "user", userMessage);

        // 2. 判斷意圖
        boolean isDrawingRequest = checkIfDrawingRequest(userMessage);

        String aiResponse;

        if (isDrawingRequest) {
            System.out.println("🤖 生圖需求...");
            ollamaService.unloadModel();
            // 這裡休息 1 秒讓顯卡喘息 (防止 VRAM 搶奪)
            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            // aiResponse 這裡會取得完整的 HTML 圖片標籤
            aiResponse = comfyUIService.generateImage(userMessage);
            
        } else {
            System.out.println("💬 聊天需求...");
            List<ChatMessage> history = chatHistoryService.getHistory(sessionId);
            aiResponse = ollamaService.callOllamaWithHistory(history);
        }

        // 3. ★ 關鍵修改：統一儲存回應 ★
        // 不管是文字還是圖片 HTML，都直接存入資料庫
        // 前端已經有能力辨識並渲染這些 HTML
        chatHistoryService.addMessage(sessionId, "assistant", aiResponse);

        return aiResponse;
    }

    private boolean checkIfDrawingRequest(String userMessage) {
        String lowerMsg = userMessage.toLowerCase();
        if (lowerMsg.startsWith("draw") || lowerMsg.contains("generate image") || 
            lowerMsg.startsWith("畫") || lowerMsg.contains("生圖")) {
            return true;
        }
        return ollamaService.callOllama(
            "Is this a drawing request? YES/NO only. Input: " + userMessage
        ).toUpperCase().contains("YES");
    }
}