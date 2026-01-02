package com.educoon.domain.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatus {

    public enum Status{
        IDLE,
        STUDYING
    }

    private Long userId;

    private String nickname;

    private String profileImageUrl;

    @Builder.Default
    private Status status = Status.IDLE;

    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer.class)
    private LocalDateTime studyStartTime;
}
