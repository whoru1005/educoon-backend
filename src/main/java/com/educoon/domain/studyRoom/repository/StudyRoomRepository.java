package com.educoon.domain.studyRoom.repository;

import com.educoon.domain.studyRoom.entity.StudyRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s from StudyRoom s WHERE s.roomId = :roomId")
    Optional<StudyRoom> findByWithLock(@Param("roomId") Long roomId);

    Page<StudyRoom> findAll(Pageable pageable);

    @Query("SELECT DISTINCT sr FROM StudyRoom sr " +
            "JOIN sr.participants rp " +
            "JOIN rp.user u " +
            "LEFT JOIN FETCH sr.roomTagMaps rtm " +
            "LEFT JOIN FETCH rtm.tag " +
            "LEFT JOIN FETCH sr.participants " +
            "WHERE u.kakaoId = :kakaoId")
    List<StudyRoom> findMyStudyRoomsByKakaoId(@Param("kakaoId") String kakaoId);

    @Query("SELECT DISTINCT sr FROM StudyRoom sr " +
            "JOIN sr.participants rp " +
            "JOIN rp.user u " +
            "LEFT JOIN FETCH sr.roomTagMaps rtm " +
            "LEFT JOIN FETCH rtm.tag " +
            "LEFT JOIN FETCH sr.participants " +
            "WHERE sr.roomId = :roomId")
    Optional<StudyRoom> findRoomDetailsById(@Param("roomId") Long roomId);

    @EntityGraph(attributePaths = {"roomTagMaps", "roomTagMaps.tag"})
    Page<StudyRoom> findByTitleContaining(String title, Pageable pageable);

    @Query("SELECT sr FROM StudyRoom sr " +
            "LEFT JOIN FETCH sr.roomTagMaps rtm " +
            "LEFT JOIN FETCH rtm.tag t " +
            "WHERE sr.roomId = :roomId")
    Optional<StudyRoom> findByIdWithTags(@Param("roomId") Long roomId);

    @Query("SELECT DISTINCT s FROM StudyRoom s " +
            "LEFT JOIN FETCH s.roomTagMaps rtm " +
            "LEFT JOIN FETCH rtm.tag t " +
            "LEFT JOIN FETCH s.participants p " +
            "WHERE s.title LIKE CONCAT('%', :keyword, '%') OR t.name LIKE CONCAT('%', :keyword, '%')")
    List<StudyRoom> findByKeywordWithTags(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT sr FROM StudyRoom sr " +
            "LEFT JOIN FETCH sr.owner " +
            "WHERE sr.roomId = :roomId")
    Optional<StudyRoom> findByIdWithOwner(@Param("roomId") Long roomId);

}
