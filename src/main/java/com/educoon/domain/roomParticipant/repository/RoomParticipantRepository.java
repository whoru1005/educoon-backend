package com.educoon.domain.roomParticipant.repository;

import com.educoon.domain.roomParticipant.entity.RoomParticipant;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {


    @Query("SELECT rp FROM RoomParticipant rp " +
            "JOIN FETCH rp.user u " +
            "WHERE rp.studyRoom.roomId = :roomId")
    List<RoomParticipant> findAllWithUserByRoomId(@Param("roomId") Long roomId);


    long countByStudyRoom(StudyRoom studyRoom);

    boolean existsByUserAndStudyRoom(User user, StudyRoom studyRoom);

    Optional<RoomParticipant> findByUserAndStudyRoom(User user, StudyRoom studyRoom);

    @Query("SELECT rp.user FROM RoomParticipant rp " +
            "WHERE rp.studyRoom = :room " +
            "AND rp.user != :owner " + // 방장 제외
            "ORDER BY rp.joinedAt ASC")
    List<User> findOldestMemberInRoom(
            @Param("room") StudyRoom room,
            @Param("owner") User owner,
            Pageable pageable // (LIMIT 1을 위해 Pageable 사용)
    );

}
