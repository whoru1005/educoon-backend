package com.educoon.domain.studyRoom;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StudyRoomUpdateRequest {

    @NotEmpty(message = "그룹명은 필수입니다")
    private String title;

    private String password;

    @NotNull(message = "모집 인원은 필수입니다")
    private Integer maxCapacity;

    @NotEmpty(message = "그룹 설명은 필수입니다")
    private String description;

    @NotNull(message = "태그 목록은 필수입니다")
    private List<Long> tagIds;
}
