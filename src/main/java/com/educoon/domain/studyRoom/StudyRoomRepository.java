package com.educoon.domain.studyRoom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    @Override
    @EntityGraph(attributePaths = {"roomTagMaps", "roomTagMaps.tag"})
    Page<StudyRoom> findAll(Pageable pageable);

    /**
     * [페이징 X / N+1 최적화 O]
     * 특정 kakaoId를 가진 사용자가 참여(participants)하고 있는 모든 스터디룸을 조회합니다.
     * * [근거]
     * 1. (필터링) User -> RoomParticipant -> StudyRoom 순으로 JOIN
     * 2. (N+1 최적화) StudyRoom -> RoomTagMap -> Tag 순으로 LEFT JOIN FETCH
     * 3. (중복 제거) DISTINCT 키워드 사용
     *
     * @param kakaoId 사용자의 카카오ID (비즈니스 키)
     * @return N+1 문제가 해결된 List<StudyRoom> -> 한번 읽어볼거
     */
    @Query("SELECT DISTINCT sr FROM StudyRoom sr " +
            "JOIN sr.participants rp " +
            "JOIN rp.user u " +
            "LEFT JOIN FETCH sr.roomTagMaps rtm " +
            "LEFT JOIN FETCH rtm.tag " +
            "WHERE u.kakaoId = :kakaoId")
    List<StudyRoom> findMyStudyRoomsByKakaoId(@Param("kakaoId") String kakaoId);
}
