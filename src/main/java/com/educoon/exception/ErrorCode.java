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
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A-005", "유효하지 않은 Refresh 토큰입니다"),
    NOT_FOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "A-006", "해당 Refresh Token을 찾을 수 없습니다"),
    MISMATCH_TOKEN_USER(HttpStatus.UNAUTHORIZED, "A-007", "토큰 소유자가 불일치합니다"),
    AI_BOT_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A-008", "AI 봇 사용자를 찾을 수 없습니다."),
    ROOM_NOT_FOUND_FOR_AI(HttpStatus.NOT_FOUND, "A-009", "AI 서비스를 위한 방을 찾을 수 없습니다."),

    // General (일반)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G-001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G-002", "유효하지 않은 입력 값입니다."),
    FORBIDDEN_ACTION(HttpStatus.FORBIDDEN, "G-003", "허락되지 않은 동작입니다"),

    // User (사용자)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U-001", "해당 사용자가 존재하지 않습니다" ),

    // StudyRoom
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "S-001", "해당 스터디룸이 존재하지 않습니다"),
    ROOM_IS_FULL(HttpStatus.BAD_REQUEST, "S-002", "스터디룸의 정원이 가득 찼습니다"),
    ALREADY_JOINED_ROOM(HttpStatus.CONFLICT, "S-003", "이미 가입한 스터디룸입니다"),
    NOT_PARTICIPANT(HttpStatus.NOT_FOUND, "S-004", "스터디룸의 가입자가 아닙니다"),
    OWNER_CANNOT_LEAVE(HttpStatus.FORBIDDEN, "S-005", "방장은 스터디룸을 탈퇴할 수 없습니다. (삭제만 가능)"),
    INVALID_ROOM_PASSWORD(HttpStatus.BAD_REQUEST, "S-006", "유효하지 않은 비밀번호입니다"),

    // 태그
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "T-001", "해당 태그가 존재하지 않습니다"),

    // 학과
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "D-001", "해당 학과가 존재하지 않습니다"),
    // 명언
    QUOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "Q-001", "등록된 명언이 없습니다."),

    // AI Storage
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "N-001", "노트를 찾을 수 없습니다."),
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "Q-002", "퀴즈를 찾을 수 없습니다."),

    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "R-001" ,"시작시간이 종료시간 이후입니다");



    private final HttpStatus httpStatus; // HTTP
    private final String code; //
    private final String message; //

}
