package com.educoon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * [CustomException]
     * 우리가 직접 정의한 CustomException을 처리합니다.
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.warn("CustomException occurred: {}", e.getMessage(), e);
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    /**
     * [MethodArgumentNotValidException]
     * 컨트롤러의 @Valid DTO 유효성 검사(예: @NotEmpty)에 실패했을 때 발생하는 예외를 처리합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        // 첫 번째 에러 필드와 메시지를 가져옴
        String fieldName = e.getBindingResult().getFieldError().getField();
        String message = e.getBindingResult().getFieldError().getDefaultMessage();

        log.warn("Validation Failed: [{}] {}", fieldName, message);

        // ErrorResponse에 상세 메시지를 담아서 보냄 (기존 ErrorResponse 구조 활용)
        // 실제로는 ErrorResponse에 errors 리스트 필드를 추가하는 것이 더 좋음
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(400)
                        .error("BAD_REQUEST")
                        .code("G-002")
                        .message(fieldName + ": " + message) // 예: "email: 이메일 형식이 올바르지 않습니다"
                        .build());
    }

    /**
     * [Exception]
     * 위에서 처리하지 못한 모든 예외(500 Error)를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred: {}", e.getMessage(), e);
        return ErrorResponse.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }


}
