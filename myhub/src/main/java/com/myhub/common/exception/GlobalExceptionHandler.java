package com.myhub.common.exception;

import com.myhub.common.result.Result;
import com.myhub.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Result<Void>> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).orElse("请求参数错误");
        return ResponseEntity.badRequest().body(Result.failure(400, message));
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<Result<Void>> business(BusinessException ex) {
        return ResponseEntity.status(ex.getCode() >= 400 && ex.getCode() < 600 ? ex.getCode() : 400)
                .body(Result.failure(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Result<Void>> authentication(AuthenticationException ex) {
        // Keep the response identical for unknown users, bad passwords, and disabled accounts.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.failure(HttpStatus.UNAUTHORIZED.value(), "用户名或密码错误"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Result<Void>> unexpected(Exception ex) {
        log.error("Unhandled request error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.failure(ResultCode.INTERNAL_ERROR));
    }
}
