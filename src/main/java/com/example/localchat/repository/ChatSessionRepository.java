package com.example.localchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.localchat.model.ChatSession;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    // 預設的 save, findById, deleteById 就夠用了
}