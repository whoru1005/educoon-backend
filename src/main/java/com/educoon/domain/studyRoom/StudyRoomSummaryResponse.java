package com.educoon.domain.studyRoom;

import com.educoon.domain.tag.TagSummaryResponse;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StudyRoomSummaryResponse {

    private final Long roomId;
    private final String title;
    private final boolean isPublic;
    private final int currentMembers;
    private final int maxCapacity;
    private final List<TagSummaryResponse> tags;

    public StudyRoomSummaryResponse(StudyRoom studyRoom){
        this.roomId = studyRoom.getRoomId();
        this.title = studyRoom.getTitle();
        this.isPublic = studyRoom.getIsPublic();
        this.currentMembers = studyRoom.getCurrentMembers();
        this.maxCapacity = studyRoom.getMaxCapacity();

        this.tags = studyRoom.getRoomTagMaps().stream()
                .map(roomTagMap -> new TagSummaryResponse(roomTagMap.getTag()))
                .collect(Collectors.toList());
    }
}
