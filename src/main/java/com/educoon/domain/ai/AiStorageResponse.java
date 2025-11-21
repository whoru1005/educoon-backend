package com.educoon.domain.ai;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiStorageResponse {
    private Long id;
    private String type;
    private String title;
    private LocalDateTime createdAt;
}
