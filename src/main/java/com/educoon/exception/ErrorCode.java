package com.educoon.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth (인증)
    INVALID_KAKAO_TOKEN(HttpStatus.UNAUTHORIZED, "A-001", "유효하지 않은 카카오 토큰입니다."),
    KAKAO_USER_INFO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A-002", "카카오 사용자 정보 조회에 실패했습니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "A-003", "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "A-004", "만료된 JWT 토큰입니다."),

    // General (일반)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G-001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G-002", "유효하지 않은 입력 값입니다."),

    // User (사용자)

    // StudyRoom
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "S-001", "해당 스터디룸이 존재하지 않습니다"),

    // 태그
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "T-001", "해당 태그가 존재하지 않습니다"),

    // 학과
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "D-001", "해당 학과가 존재하지 않습니다");



    private final HttpStatus httpStatus; // HTTP
    private final String code; //
    private final String message; //

}
