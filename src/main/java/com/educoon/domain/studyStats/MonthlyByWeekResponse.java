package com.educoon.domain.studyStats; // (패키지는 동일하게 맞춰주세요)

import lombok.Getter;

@Getter
public class MonthlyByWeekResponse {

    private Integer weekOfMonth; // 1, 2, 3, 4, 5 (월 기준 주차)
    private Long totalDuration;

    public MonthlyByWeekResponse(Integer weekOfMonth, Long totalDuration) {
        this.weekOfMonth = weekOfMonth;
        this.totalDuration = (totalDuration != null) ? totalDuration : 0L;
    }
}