package com.educoon.domain.chatMessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop50ByStudyRoomRoomIdOrderByTimestampDesc(Long roomId);

    @Query(value = "SELECT cm FROM ChatMessage cm " +
            "JOIN FETCH cm.user " +
            "WHERE cm.studyRoom.roomId = :roomId",
            countQuery = "SELECT count(cm) FROM ChatMessage cm WHERE cm.studyRoom.roomId = :roomId")
    Page<ChatMessage> findAllByStudyRoomRoomIdOrderByTimestampDesc(Long roomId, Pageable pageable);
}
