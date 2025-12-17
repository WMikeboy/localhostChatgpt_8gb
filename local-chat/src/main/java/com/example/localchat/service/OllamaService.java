package com.example.localchat.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.localchat.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class OllamaService {

    // Important: Endpoint changed from /api/generate to /api/chat
    // This allows the handling of conversation history (Context)
    private static final String OLLAMA_CHAT_API_URL = "http://localhost:11434/api/chat";
    
    // Use the traditional /api/generate for VRAM release
    private static final String OLLAMA_GENERATE_API_URL = "http://localhost:11434/api/generate";
    
    private static final String MODEL_NAME = "qwen2.5:3b";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Call Ollama with conversation history
     */
    public String callOllamaWithHistory(List<ChatMessage> history) {
        try {
            // 1. Construct JSON Request
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", MODEL_NAME);
            rootNode.put("stream", false);

            // Convert Java List<ChatMessage> to JSON Array
            // Format: "messages": [ {"role": "user", "content": "..."}, ... ]
            ArrayNode messagesNode = rootNode.putArray("messages");
            for (ChatMessage msg : history) {
                ObjectNode msgNode = messagesNode.addObject();
                msgNode.put("role", msg.getRole());
                msgNode.put("content", msg.getContent());
            }

            String jsonBody = objectMapper.writeValueAsString(rootNode);
            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(OLLAMA_CHAT_API_URL)
                    .post(body)
                    .build();

            // 2. Send Request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "Error: Ollama API call failed with code=" + response.code();
                }
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                // The response structure for /api/chat is "message" -> "content"
                // (Note: This differs from the "response" field in /api/generate)
                if (jsonNode.has("message") && jsonNode.get("message").has("content")) {
                    return jsonNode.get("message").get("content").asText();
                } else {
                    return "Error: Unexpected response format";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Method for single-turn calls (used for intent detection in LlmRouter)
     * Wraps the callOllamaWithHistory method for consistency
     */
    public String callOllama(String msg) {
        // Wrap the single message in a list and pass it to the history-enabled method
        return callOllamaWithHistory(List.of(new ChatMessage("user", msg)));
    }

    /**
     * Method to force release VRAM
     */
    public void unloadModel() {
        try {
            String jsonBody = String.format("{\"model\": \"%s\", \"keep_alive\": 0}", MODEL_NAME);
            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(OLLAMA_GENERATE_API_URL) // Using the generate API for unloading is more reliable
                    .post(body)
                    .build();
            
            client.newCall(request).execute().close();
            System.out.println("VRAM for Ollama has been forcibly released");
            
        } catch (Exception e) {
            System.err.println("Failed to release memory: " + e.getMessage());
        }
    }
}