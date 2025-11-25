package com.educoon.domain.studyRecord;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudyRecordCreateRequest {

    @NotNull(message = "공부 시작 시간은 필수입니다")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", timezone = "Asia/Seoul")
    private LocalDateTime startTime;

    @NotNull(message = "공부 종료 시간은 필수입니다")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", timezone = "Asia/Seoul")
    private LocalDateTime endTime;

    @NotNull(message = "공부 시간(초)은 필수입니다")
    @Positive(message = "공부 시간은 0보다 커야합니다")
    private Long duration;

    private Long roomId;

}
