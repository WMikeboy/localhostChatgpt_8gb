package com.example.localchat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.localchat.model.ChatMessage;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    // Derived Query 1: Find all messages for a specific Session, ordered by time
    // Corresponding SQL: SELECT * FROM chat_messages WHERE session_id = ? ORDER BY created_at ASC
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    // Derived Query 2: Count how many messages are currently stored in a specific Session
    // Corresponding SQL: SELECT COUNT(*) FROM chat_messages WHERE session_id = ?
    long countBySessionId(String sessionId);
    
    // Derived Query 3: Delete all records for a specific Session (used for clearing memory)
    // Corresponding SQL: DELETE FROM chat_messages WHERE session_id = ?
    void deleteBySessionId(String sessionId);
    
    // Derived Query 4: Find the oldest message in a Session (used for sliding window memory/eviction)
    ChatMessage findFirstBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    //  Added: Find all unique Session IDs, ordered by the latest activity time
    @Query("SELECT c.sessionId FROM ChatMessage c GROUP BY c.sessionId ORDER BY MAX(c.createdAt) DESC")
    List<String> findAllSessionIds();
}