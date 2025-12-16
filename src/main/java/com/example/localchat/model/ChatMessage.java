package com.example.localchat.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private LocalDateTime createdAt;

    // 1. 這是原本的 3 參數建構子 (給一般聊天用)
    public ChatMessage(String sessionId, String role, String content) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // 2. ★ 請補上這個 2 參數建構子 (給 OllamaService 內部意圖偵測用) ★
    // 我們自動補上一個 "system" 作為 sessionId，避免程式報錯
    public ChatMessage(String role, String content) {
        this.sessionId = "system-internal"; // 預設一個內部 ID
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}