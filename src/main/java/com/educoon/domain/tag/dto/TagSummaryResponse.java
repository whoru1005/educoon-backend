package com.educoon.domain.tag.dto;

import com.educoon.domain.tag.entity.Tag;
import lombok.Getter;

@Getter
public class TagSummaryResponse {

    private final Long tagId;
    private final String name;

    public TagSummaryResponse(Tag tag){
        this.tagId = tag.getTagId();
        this.name = tag.getName();
    }
}
