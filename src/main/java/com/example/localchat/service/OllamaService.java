package com.example.localchat.service;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.localchat.model.ChatMessage; // 👈 必須確認這行沒有紅底線
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

    // ⚠️ 重要: エンドポイントを /api/generate から /api/chat に変更
    // これにより、会話履歴 (Context) を扱えるようになります
    private static final String OLLAMA_CHAT_API_URL = "http://localhost:11434/api/chat";
    
    // VRAM解放用は従来の /api/generate を使います
    private static final String OLLAMA_GENERATE_API_URL = "http://localhost:11434/api/generate";
    
    private static final String MODEL_NAME = "qwen2.5:3b";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 新しいメソッド: 会話履歴付きで Ollama を呼び出す
     */
    public String callOllamaWithHistory(List<ChatMessage> history) {
        try {
            // 1. JSON Request の構築
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", MODEL_NAME);
            rootNode.put("stream", false);

            // Java の List<ChatMessage> を JSON Array に変換
            // 形式: "messages": [ {"role": "user", "content": "..."}, ... ]
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

            // 2. リクエスト送信
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "エラー: Ollama API 呼び出し失敗 code=" + response.code();
                }
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                // /api/chat のレスポンス構造は "message" -> "content" です
                // (/api/generate の "response" とは異なるので注意)
                if (jsonNode.has("message") && jsonNode.get("message").has("content")) {
                    return jsonNode.get("message").get("content").asText();
                } else {
                    return "エラー: 想定外のレスポンス形式です";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "エラー: " + e.getMessage();
        }
    }

    /**
     * 既存の単発呼び出し用メソッド（LlmRouterの意図判定などで使用）
     * 内部で callOllamaWithHistory を再利用するように書き換えます
     */
    public String callOllama(String msg) {
        // 単発のメッセージをリストに入れて、履歴付きメソッドに渡す
        return callOllamaWithHistory(List.of(new ChatMessage("user", msg)));
    }

    /**
     * VRAM 強制解放用メソッド (変更なし)
     */
    public void unloadModel() {
        try {
            String jsonBody = String.format("{\"model\": \"%s\", \"keep_alive\": 0}", MODEL_NAME);
            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(OLLAMA_GENERATE_API_URL) // 解放は generate API で行うのが確実
                    .post(body)
                    .build();
            
            client.newCall(request).execute().close();
            System.out.println("🧹 已強制釋放 Ollama VRAM");
            
        } catch (Exception e) {
            System.err.println("釋放記憶體失敗: " + e.getMessage());
        }
    }
}