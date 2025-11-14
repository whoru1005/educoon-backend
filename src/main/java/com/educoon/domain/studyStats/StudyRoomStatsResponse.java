package com.educoon.domain.studyStats;

import com.educoon.domain.studyRoom.StudyRoom;
import lombok.Getter;

@Getter
public class StudyRoomStatsResponse {

    private Long roomId;
    private String roomTitle;
    private Long totalDuration;

    /**
     * StudyRecordRepository의 JPQL 쿼리에서
     * 'new com.educoon.domain.studyStats.RoomStatsResponse(s.studyRoom, SUM(s.duration))'
     * 형식으로 호출하기 위한 생성자입니다.
     */
    public StudyRoomStatsResponse(StudyRoom studyRoom, Long totalDuration) {
        this.roomId = studyRoom.getRoomId();
        this.roomTitle = studyRoom.getTitle(); // (StudyRoom 엔티티의 title 필드)
        this.totalDuration = (totalDuration != null) ? totalDuration : 0L;
    }
}