package com.educoon.domain.studyStats;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StudyStatsTotalDurationResponse {

    private Long totalDuration; // (단위: 초)

    /**
     * JPQL의 SUM()이 0건일 때 null을 반환할 수 있으므로,
     * 생성자에서 null을 0L로 처리해줍니다.
     */
    public StudyStatsTotalDurationResponse(Long totalDuration) {
        this.totalDuration = (totalDuration != null) ? totalDuration : 0L;
    }

}
