package com.example.localchat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.localchat.model.ChatMessage;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    // 魔法方法 1：找出某個 Session 的所有對話，並依照時間排序
    // 對應 SQL: SELECT * FROM chat_messages WHERE session_id = ? ORDER BY created_at ASC
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    // 魔法方法 2：計算某個 Session 目前存了幾句話
    // 對應 SQL: SELECT COUNT(*) FROM chat_messages WHERE session_id = ?
    long countBySessionId(String sessionId);
    
    // 魔法方法 3：刪除該 Session 的所有紀錄 (清空記憶用)
    // 對應 SQL: DELETE FROM chat_messages WHERE session_id = ?
    void deleteBySessionId(String sessionId);
    
    // 魔法方法 4：找出該 Session 最舊的一筆 (用來刪除多餘記憶，維持短期記憶窗格)
    ChatMessage findFirstBySessionIdOrderByCreatedAtAsc(String sessionId);
    
 // ★ 新增：找出所有不重複的 Session ID，並依照最新活動時間排序
    @Query("SELECT c.sessionId FROM ChatMessage c GROUP BY c.sessionId ORDER BY MAX(c.createdAt) DESC")
    List<String> findAllSessionIds();
}