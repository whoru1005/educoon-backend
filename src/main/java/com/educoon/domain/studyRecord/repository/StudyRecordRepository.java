package com.educoon.domain.studyRecord.repository;

import com.educoon.domain.studyRecord.entity.StudyRecord;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyStats.DailyStudyStatsResponse;
import com.educoon.domain.studyStats.MonthlyStudyStatsResponse;
import com.educoon.domain.studyStats.StudyRoomStatsResponse;
import com.educoon.domain.studyStats.WeeklyStudyStatsResponse;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.dto.UserDuration;
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

    @Query("SELECT new com.educoon.domain.studyStats.DailyStudyStatsResponse(CAST(s.startTime AS LocalDate), SUM(s.duration)) " + // [ "FUNCTION" -> "CAST" ]
            "FROM StudyRecord s " +
            "WHERE s.user = :user " +
            "AND s.startTime BETWEEN :start AND :end " +
            "GROUP BY CAST(s.startTime AS LocalDate) " +      // [ "FUNCTION" -> "CAST" ]
            "ORDER BY CAST(s.startTime AS LocalDate) ASC")
    List<DailyStudyStatsResponse> findDailyStudyStatsByUserAndPeriod(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT new com.educoon.domain.studyStats.WeeklyStudyStatsResponse(WEEK(s.startTime), SUM(s.duration)) " +
            "FROM StudyRecord s " +
            "WHERE s.user = :user " +
            "AND s.startTime BETWEEN :start AND :end " +
            "GROUP BY WEEK(s.startTime) " +
            "ORDER BY WEEK(s.startTime) ASC") // 주차순 정렬
    List<WeeklyStudyStatsResponse> findWeeklyDurationSumByUserAndPeriod(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query("SELECT new com.educoon.domain.studyStats.MonthlyStudyStatsResponse(MONTH(s.startTime), SUM(s.duration)) " + // [ "FUNCTION('MONTH', ...)" -> "MONTH(...)" ]
            "FROM StudyRecord s " +
            "WHERE s.user = :user " +
            "AND s.startTime BETWEEN :start AND :end " +
            "GROUP BY MONTH(s.startTime) " +      // [ "FUNCTION('MONTH', ...)" -> "MONTH(...)" ]
            "ORDER BY MONTH(s.startTime) ASC")    // [ "FUNCTION('MONTH', ...
    List<MonthlyStudyStatsResponse> findMonthlyDurationSumByUserAndPeriod(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT new com.educoon.domain.user.UserDuration(s.user, SUM(s.duration)) " +
            "FROM StudyRecord s " +
            "WHERE s.studyRoom = :room " +
            "AND s.user != :owner " + // 방장 제외
            "AND s.startTime BETWEEN :start AND :end " +
            "GROUP BY s.user " +
            "ORDER BY SUM(s.duration) DESC")
    List<UserDuration> findTopStudierInRoom(
            @Param("room") StudyRoom room,
            @Param("owner") User owner,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
