package com.educoon.domain.studyStats.dto;

import com.educoon.domain.studyRoom.entity.StudyRoom;
import lombok.Getter;

@Getter
public class StudyRoomStatsResponse {

    private Long roomId;
    private String roomTitle;
    private Long totalDuration;

    /**
     * StudyRecordRepository의 JPQL 쿼리에서
     * 'new com.educoon.domain.studyStats.dto.StudyRoomStatsResponse(s.studyRoom, SUM(s.duration))'
     * 형식으로 호출하기 위한 생성자입니다.
     */
    public StudyRoomStatsResponse(Long roomId, String roomTitle, Long totalDuration) {
        this.roomId = roomId;
        this.roomTitle = roomTitle;
        this.totalDuration = (totalDuration != null) ? totalDuration : 0L;
    }
}
