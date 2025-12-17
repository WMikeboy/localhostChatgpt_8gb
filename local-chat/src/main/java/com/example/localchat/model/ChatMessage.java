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

    // 1. Original 3-parameter constructor (for standard chat usage)
    public ChatMessage(String sessionId, String role, String content) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // 2.  2-parameter constructor (for internal intent detection in OllamaService) ★
    // Automatically assigns "system-internal" as the sessionId to prevent application errors
    public ChatMessage(String role, String content) {
        this.sessionId = "system-internal"; // Default internal ID
        this.role = role;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}