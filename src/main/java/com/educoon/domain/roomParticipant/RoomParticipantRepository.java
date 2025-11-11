package com.educoon.domain.roomParticipant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {


    @Query("SELECT rp FROM RoomParticipant rp " +
            "JOIN FETCH rp.user u " +
            "WHERE rp.studyRoom.roomId = :roomId")
    List<RoomParticipant> findAllWithUserByRoomId(@Param("roomId") Long roomId);
}
