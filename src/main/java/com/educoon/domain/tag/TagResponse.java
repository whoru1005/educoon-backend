package com.educoon.domain.tag;

import com.educoon.domain.tagCategory.TagCategory;
import lombok.Getter;

@Getter
public class TagResponse {
    private Long tagId;
    private String name;
    private TagCategory tagCategory;

    public TagResponse(Long tagId, String name, TagCategory tagCategory) {
        this.tagId = tagId;
        this.name = name;
        this.tagCategory = tagCategory;
    }

}
