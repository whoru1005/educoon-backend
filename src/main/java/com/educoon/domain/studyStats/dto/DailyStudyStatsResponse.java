package com.educoon.domain.studyStats.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DailyStudyStatsResponse {

    private LocalDate date;
    private Long totalDuration;

    public DailyStudyStatsResponse(LocalDate date, Long totalDuration) {
        this.date = date;
        this.totalDuration = (totalDuration != null) ? totalDuration : 0L;
    }
}
