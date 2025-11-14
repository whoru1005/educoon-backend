package com.educoon.domain.studyStats;

import com.educoon.domain.studyRecord.StudyRecordRepository;
import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.studyRoom.StudyRoomRepository;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
}

