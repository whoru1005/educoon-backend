package com.educoon.domain.chatMessage.repository;

import com.educoon.domain.chatMessage.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm " +
            "JOIN FETCH cm.user " +
            "WHERE cm.studyRoom.roomId = :roomId " +
            "ORDER BY cm.timestamp DESC " +
            "LIMIT 50")
    List<ChatMessage> findTop50ByStudyRoomRoomIdOrderByTimestampDesc(Long roomId);

    @Query(value = "SELECT cm FROM ChatMessage cm " +
            "JOIN FETCH cm.user " +
            "WHERE cm.studyRoom.roomId = :roomId" +
            " ORDER BY cm.timestamp DESC",
            countQuery = "SELECT count(cm) FROM ChatMessage cm WHERE cm.studyRoom.roomId = :roomId")
    Page<ChatMessage> findAllByStudyRoomRoomIdOrderByTimestampDesc(Long roomId, Pageable pageable);
}
