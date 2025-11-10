package com.educoon.domain.studyRoom;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StudyRoomCreateRequest {

    private String title;
    private String description;
    private int maxCapacity;
    private String password;
    private List<Long> tagIds;
}
