package com.educoon.domain.studyRecord.dto;

import com.educoon.domain.studyRecord.entity.StudyRecord;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudyRecordResponse {

    private Long recordId;
    private Long roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;

    public StudyRecordResponse(StudyRecord studyRecord){
        this.recordId = studyRecord.getRecordId();
        this.roomId = (studyRecord.getStudyRoom() != null) ? studyRecord.getStudyRoom().getRoomId() : null;
        this.startTime = studyRecord.getStartTime();
        this.endTime = studyRecord.getEndTime();
        this.duration = studyRecord.getDuration();
    }
}
