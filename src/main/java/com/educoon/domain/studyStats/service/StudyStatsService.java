package com.educoon.domain.studyStats.service;

import com.educoon.domain.studyRecord.repository.StudyRecordRepository;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.repository.StudyRoomRepository;
import com.educoon.domain.studyStats.dto.*;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyStatsService {

    private final UserRepository userRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final StudyRecordRepository studyRecordRepository;

    public List<StudyRoomStatsResponse> getDailyRoomStats(
            String kakaoId, LocalDate date, Long roomId) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime todayStart = date.atStartOfDay();
        LocalDateTime todayEnd = date.atTime(LocalTime.MAX);

        // [ 2. "최종 결정"에 따른 분기 로직 ]
        if (roomId == null) {
            // 2-1. (ED-103) roomId가 없으면: 방별 그룹 쿼리 호출
            return studyRecordRepository.findRoomDurationSumByUserAndPeriod(
                    user, todayStart, todayEnd
            );
        } else {
            // 2-2. (ED-101) roomId가 있으면: 특정 방 1개 쿼리 호출
            StudyRoom room = studyRoomRepository.findById(roomId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

            return studyRecordRepository.findSingleRoomDurationSumByUserAndPeriod(
                    user, room, todayStart, todayEnd
            );
        }
    }

    /**
     * [ED-102] 일간 총 누적 공부 시간
     */
    public StudyStatsTotalDurationResponse getDailyTotalDuration(String kakaoId, LocalDate date) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime todayStart = date.atStartOfDay();
        LocalDateTime todayEnd = date.atTime(LocalTime.MAX);

        // (기존 쿼리 재사용)
        Long totalDuration = studyRecordRepository.findDurationSumByUserAndPeriod(
                user, todayStart, todayEnd
        );

        return new StudyStatsTotalDurationResponse(totalDuration);
    }

    public StudyStatsTotalDurationResponse getWeeklyTotalDuration(String kakaoId, LocalDate date){

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfWeek = date.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = date.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

        Long totalDuration = studyRecordRepository.findDurationSumByUserAndPeriod(user, startOfWeek, endOfWeek);

        return new StudyStatsTotalDurationResponse(totalDuration);
    }

    public List<StudyRoomStatsResponse> getWeeklyRoomStats(String kakaoId, LocalDate date) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfWeek = date.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = date.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

        return studyRecordRepository.findRoomDurationSumByUserAndPeriod(
                user, startOfWeek, endOfWeek
        );
    }

    /**
     * [ED-106] 해당 주차 일별 누적 공부 시간
     */
    public List<DailyStudyStatsResponse> getWeeklyDailyStats(String kakaoId, LocalDate date) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfWeek = date.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = date.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

        return studyRecordRepository.findDailyStudyStatsByUserAndPeriod(
                user, startOfWeek, endOfWeek
        );
    }

    public StudyStatsTotalDurationResponse getMonthlyTotalDuration(String kakaoId, LocalDate date){

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        Long totalDuration = studyRecordRepository.findDurationSumByUserAndPeriod(user, startOfMonth, endOfMonth);

        return new StudyStatsTotalDurationResponse(totalDuration);
    }

    public List<StudyRoomStatsResponse> getMonthlyRoomStats(String kakaoId, LocalDate date){

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        return studyRecordRepository.findRoomDurationSumByUserAndPeriod(user, startOfMonth, endOfMonth);
    }

    public List<MonthlyByWeekResponse> getMonthlyWeeklyStats(String kakaoId, LocalDate date) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


// 1. [DB 조회] 해당 월의 1일 ~ 마지막 날 계산
        LocalDateTime startOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        // 2. [DB 조회] Repository는 "연 기준 주차(weekOfYear)"로 그룹핑된 데이터를 반환
        // (WeeklyStatsResponse DTO 재사용)
        List<WeeklyStudyStatsResponse> statsByWeekOfYear =
                studyRecordRepository.findWeeklyDurationSumByUserAndPeriod(
                        user, startOfMonth, endOfMonth
                );

        // 3. [Java 변환] "연 기준 주차" -> "월 기준 주차"로 변환

        // (참고: 주의 시작을 월요일로 하는 ISO 표준 사용)
        WeekFields weekFields = WeekFields.of(Locale.KOREA); // (또는 Locale.getDefault())

        // 이 달의 1일이 1년 중 몇 번째 주인지 계산 (e.g., 11/1일 -> 45주차)
        int firstWeekNumOfMonth = startOfMonth.get(weekFields.weekOfYear());

        return statsByWeekOfYear.stream()
                .map(stat -> {
                    // (e.g., 46주차 - 45주차 + 1 = 2번째 주)
                    int weekOfMonth = stat.getWeekOfYear() - firstWeekNumOfMonth + 1;
                    return new MonthlyByWeekResponse(weekOfMonth, stat.getTotalDuration());
                })
                .collect(Collectors.toList());
    }


    public StudyStatsTotalDurationResponse getYearlyTotalDuration(String kakaoId, LocalDate date) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfYear = date.withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = date.with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);

        Long totalDuration = studyRecordRepository.findDurationSumByUserAndPeriod(
                user, startOfYear, endOfYear
        );

        return new StudyStatsTotalDurationResponse(totalDuration);
    }

    /**
     * [ED-110] 해당 연 월별 누적 공부 시간
     */
    public List<MonthlyStudyStatsResponse> getYearlyMonthlyStats(String kakaoId, LocalDate date) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfYear = date.withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = date.with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);

        return studyRecordRepository.findMonthlyDurationSumByUserAndPeriod(
                user, startOfYear, endOfYear
        );
    }

    public List<StudyRoomStatsResponse> getYearlyRoomStats(String kakaoId, LocalDate date) {

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfYear = date.withDayOfYear(1).atStartOfDay();
        LocalDateTime endOfYear = date.with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);

        // [쿼리 재사용] (ED-103, ED-105, ED-108과 동일한 쿼리 사용)
        return studyRecordRepository.findRoomDurationSumByUserAndPeriod(
                user, startOfYear, endOfYear
        );
    }
}

