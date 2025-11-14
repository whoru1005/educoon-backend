package com.educoon.domain.studyRecord;

import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.studyStats.DailyStudyStatsResponse;
import com.educoon.domain.studyStats.StudyRoomStatsResponse;
import com.educoon.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {


    @Query("SELECT SUM(s.duration) FROM StudyRecord s " +
            "WHERE s.user = :user " +
            "AND s.startTime BETWEEN :start AND :end")
    Long findDurationSumByUserAndPeriod(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT new com.educoon.domain.studyStats.StudyRoomStatsResponse(s.studyRoom, SUM(s.duration)) " + // [ 1. new (패키지 경로) 추가 ]
            "FROM StudyRecord s " +
            "WHERE s.user = :user " +
            "AND s.startTime BETWEEN :start AND :end " +
            "AND s.studyRoom IS NOT NULL " +
            "GROUP BY s.studyRoom")
    List<StudyRoomStatsResponse> findRoomDurationSumByUserAndPeriod(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT new com.educoon.domain.studyStats.StudyRoomStatsResponse(s.studyRoom, SUM(s.duration)) " + // [ 2. new (패키지 경로) 추가 ]
            "FROM StudyRecord s " +
            "WHERE s.user = :user " +
            "AND s.studyRoom = :room " +
            "AND s.startTime BETWEEN :start AND :end " +
            "AND s.studyRoom IS NOT NULL " +
            "GROUP BY s.studyRoom")
    List<StudyRoomStatsResponse> findSingleRoomDurationSumByUserAndPeriod(
            @Param("user") User user,
            @Param("room") StudyRoom room,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT new com.educoon.domain.studyStats.DailyStudyStatsResponse(FUNCTION('DATE', s.startTime), SUM(s.duration)) " + // [ 3. new (패키지 경로) 추가 ]
            "FROM StudyRecord s " +
            "WHERE s.user = :user " +
            "AND s.startTime BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', s.startTime) " +
            "ORDER BY FUNCTION('DATE', s.startTime) ASC")
    List<DailyStudyStatsResponse> findDailyStudyStatsByUserAndPeriod(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
