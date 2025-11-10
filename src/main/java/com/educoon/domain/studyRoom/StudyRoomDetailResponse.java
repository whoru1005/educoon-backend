package com.educoon.domain.studyRoom;

import com.educoon.domain.tag.TagSummaryResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class StudyRoomDetailResponse {

    private final Long roomId;

    private final String title;

    private final String description;

    private final int currentMembers;

    private final int maxCapacity;

    private final boolean isPublic;

    private final List<TagSummaryResponse> tags;

    public StudyRoomDetailResponse(StudyRoom studyRoom){
        this.roomId = studyRoom.getRoomId();
        this.title = studyRoom.getTitle();
        this.description = studyRoom.getDescription();
        this.currentMembers = studyRoom.getParticipants().size();
        this.maxCapacity = studyRoom.getMaxCapacity();
        this.isPublic = studyRoom.getIsPublic();

        this.tags = studyRoom.getRoomTagMaps().stream()
                .map(roomTagMap -> new TagSummaryResponse(roomTagMap.getTag()))
                .toList();
    }
}
