package com.educoon.domain.tag;

import com.educoon.domain.tagCategory.TagCategory;
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
