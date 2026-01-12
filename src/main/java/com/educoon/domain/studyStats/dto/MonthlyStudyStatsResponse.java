package com.educoon.domain.studyStats.dto;

import lombok.Getter;

@Getter
public class MonthlyStudyStatsResponse {

    private Integer month;
    private Long totalDuration;

    public MonthlyStudyStatsResponse(Integer month, Long totalDuration){
        this.month = month;
        this.totalDuration = (totalDuration != null) ? totalDuration : 0L;
    }
}
