package com.educoon.domain.studyStats;

public class WeeklyStudyStatsResponse {
    private Integer weekOfYear;    // 1년 기준 몇 번째 주 (e.g., 45, 46)
    private Long totalDuration;  // 해당 주의 총 공부 시간 (초)

    public WeeklyStudyStatsResponse(Integer weekOfYear, Long totalDuration) {
        this.weekOfYear = weekOfYear;
        this.totalDuration = (totalDuration != null) ? totalDuration : 0L;
    }
}
