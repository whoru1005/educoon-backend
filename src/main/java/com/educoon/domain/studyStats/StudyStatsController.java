package com.educoon.domain.studyStats;

import com.educoon.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studystats")
public class StudyStatsController {

    private final StudyStatsService studyStatsService;
    private final LocalDate DEFAULT_DATE = LocalDate.now();

    @GetMapping("/daily")
    public ResponseEntity<List<StudyRoomStatsResponse>> getDailyRoomStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false) // [ 1. roomId 파라미터 추가 ]
            Long roomId
    ) {
        String kakaoId = SecurityUtils.getCurrentUserKakaoId();
        LocalDate targetDate = Optional.ofNullable(date).orElse(LocalDate.now());

        // [ 2. 서비스 로직 수정 (roomId 전달) ]
        List<StudyRoomStatsResponse> response = studyStatsService.getDailyRoomStats(
                kakaoId, targetDate, roomId
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly/total")
    public ResponseEntity<StudyStatsTotalDurationResponse> getWeeklyTotalStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        String kakaoId = SecurityUtils.getCurrentUserKakaoId();
        LocalDate targetDate = Optional.ofNullable(date).orElse(DEFAULT_DATE);

        StudyStatsTotalDurationResponse response = studyStatsService.getWeeklyTotalDuration(
                kakaoId, targetDate
        );
        return ResponseEntity.ok(response);
    }

    /**
     * [ED-105] 주간 공부방별 누적 공부 시간
     * @param date (선택) YYYY-MM-DD. 없으면 이번 주.
     */
    @GetMapping("/weekly")
    public ResponseEntity<List<StudyRoomStatsResponse>> getWeeklyRoomStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        String kakaoId = SecurityUtils.getCurrentUserKakaoId();
        LocalDate targetDate = Optional.ofNullable(date).orElse(DEFAULT_DATE);

        List<StudyRoomStatsResponse> response = studyStatsService.getWeeklyRoomStats(
                kakaoId, targetDate
        );
        return ResponseEntity.ok(response);
    }

    /**
     * [ED-106] 해당 주차 일별 누적 공부 시간
     * @param date (선택) YYYY-MM-DD. 없으면 이번 주.
     */
    @GetMapping("/weekly/by-day")
    public ResponseEntity<List<DailyStudyStatsResponse>> getWeeklyDailyStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        String kakaoId = SecurityUtils.getCurrentUserKakaoId();
        LocalDate targetDate = Optional.ofNullable(date).orElse(DEFAULT_DATE);

        List<DailyStudyStatsResponse> response = studyStatsService.getWeeklyDailyStats(
                kakaoId, targetDate
        );
        return ResponseEntity.ok(response);
    }
}
