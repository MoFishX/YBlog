package com.yvmoux.blog.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getCode()).body(Result.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return ResponseEntity.status(400).body(Result.error(400, message));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Object>> handleNotLoginException(NotLoginException e) {
        return ResponseEntity.status(401).body(Result.error(401, "未登录"));
    }

    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<Result<Object>> handleNotRoleException(NotRoleException e) {
        return ResponseEntity.status(403).body(Result.error(403, "无权限"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return ResponseEntity.status(500).body(Result.error(500, "服务器内部错误"));
    }
}
