package com.educoon.domain.studyRoom.dto;

import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.tag.dto.TagResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class StudyRoomDetailResponse {

    private final Long roomId;

    private final String title;

    private final String password;

    private final String description;

    private final int currentMembers;

    private final int maxCapacity;

    private final boolean isPublic;

    private final List<TagResponse> tags;

    public StudyRoomDetailResponse(StudyRoom studyRoom){
        this.roomId = studyRoom.getRoomId();
        this.title = studyRoom.getTitle();
        this.password = studyRoom.getPassword();
        this.description = studyRoom.getDescription();
        this.currentMembers = studyRoom.getParticipants().size();
        this.maxCapacity = studyRoom.getMaxCapacity();
        this.isPublic = studyRoom.getIsPublic();

        this.tags = studyRoom.getRoomTagMaps().stream()
                .map(roomTagMap -> new TagResponse(roomTagMap.getTag()))
                .toList();
    }
}
