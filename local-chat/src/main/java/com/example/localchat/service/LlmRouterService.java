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
        
        // 1. First, save the user's message to the history
        chatHistoryService.addMessage(sessionId, "user", userMessage);

        // 2. Determine intent
        boolean isDrawingRequest = checkIfDrawingRequest(userMessage);

        String aiResponse;

        if (isDrawingRequest) {
            System.out.println("Processing image generation request...");
            ollamaService.unloadModel();
            
            // Wait for 1 second to allow VRAM to clear (prevents GPU memory conflicts)
            try { Thread.sleep(1000); } catch (InterruptedException e) {}

            // aiResponse will contain the complete HTML image tag here
            aiResponse = comfyUIService.generateImage(userMessage);
            
        } else {
            System.out.println("Processing chat request...");
            List<ChatMessage> history = chatHistoryService.getHistory(sessionId);
            aiResponse = ollamaService.callOllamaWithHistory(history);
        }

        // 3. Key Modification: Unify response storage
        // Save the result to the database regardless of whether it is plain text or an HTML image tag.
        // The frontend is already capable of identifying and rendering these HTML tags.
        chatHistoryService.addMessage(sessionId, "assistant", aiResponse);

        return aiResponse;
    }

    private boolean checkIfDrawingRequest(String userMessage) {
        String lowerMsg = userMessage.toLowerCase();
        
        // Keyword check for both English and Chinese
        if (lowerMsg.startsWith("draw") || lowerMsg.contains("generate image") || 
            lowerMsg.startsWith("畫") || lowerMsg.contains("生圖")) {
            return true;
        }
        
        // LLM-based intent detection fallback
        return ollamaService.callOllama(
            "Is this a drawing request? YES/NO only. Input: " + userMessage
        ).toUpperCase().contains("YES");
    }
}