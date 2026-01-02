package com.educoon.domain.tag.dto;

import com.educoon.domain.tag.entity.Tag;
import com.educoon.domain.tag.entity.TagCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagResponse {
    private Long tagId;
    private String name;
    private TagCategory tagCategory;

    public TagResponse(Tag tag) {
        this.tagId = tag.getTagId();
        this.name = tag.getName();
        this.tagCategory = tag.getCategory();
    }

}
