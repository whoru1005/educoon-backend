package com.educoon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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
        BindingResult bindingResult = e.getBindingResult();


        if (bindingResult.hasFieldErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            log.warn("Validation Failed: {}", errorMessage);

            return ResponseEntity
                    .status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                    .body(ErrorResponse.builder()
                            .status(400)
                            .error("BAD_REQUEST")
                            .code("G-002")
                            .message(errorMessage)
                            .build());
        }

        // 필드 에러가 없는 경우 (글로벌 에러)
        log.warn("Validation Failed: {}", bindingResult.getGlobalError());
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_INPUT_VALUE);
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
