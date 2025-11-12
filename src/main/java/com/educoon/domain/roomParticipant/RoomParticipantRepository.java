package com.educoon.domain.roomParticipant;

import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.user.User;
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

}
