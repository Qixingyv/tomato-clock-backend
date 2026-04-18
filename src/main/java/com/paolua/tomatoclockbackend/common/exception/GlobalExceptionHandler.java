package com.paolua.tomatoclockbackend.common.exception;

import com.paolua.tomatoclockbackend.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理应用中抛出的各类异常，返回标准的响应格式
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 业务异常通常是由于请求参数不符合业务规则导致的
     *
     * @param e 业务异常
     * @return 响应实体，包含错误码和错误消息
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.badRequest()
                .body(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * 处理系统异常
     * 捕获所有未被特定处理器处理的异常
     *
     * @param e 系统异常
     * @return 响应实体，返回500错误码
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(500)
                .body(Result.error(500, "系统异常，请稍后重试"));
    }

    /**
     * 处理参数校验异常
     * 当使用 @Valid 注解进行参数校验失败时触发
     *
     * @param e 方法参数校验异常
     * @return 响应实体，包含所有校验错误消息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", message);
        return ResponseEntity.badRequest()
                .body(Result.error(400, message));
    }

    /**
     * 处理HTTP方法不支持异常
     * 当请求的HTTP方法不被支持时触发
     *
     * @param e     HTTP方法不支持异常
     * @param request HTTP请求
     * @return 响应实体，返回405错误码
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<?>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {
        String message = String.format("不支持的请求方法：%s，URI：%s", e.getMethod(), request.getRequestURI());
        log.warn("{}", message);
        return ResponseEntity.status(405)
                .body(Result.error(405, message));
    }

    /**
     * 处理路径未找到异常
     * 当请求的路径不存在时触发
     *
     * @param e 路径未找到异常
     * @return 响应实体，返回404错误码
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<?>> handleNotFound(NoHandlerFoundException e) {
        String message = String.format("请求路径不存在：%s", e.getRequestURL());
        log.warn("{}", message);
        return ResponseEntity.status(404)
                .body(Result.error(404, message));
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @return 响应实体，返回400错误码
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<?>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数：{}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Result.error(400, e.getMessage()));
    }

    /**
     * 处理运行时异常
     * 捕获未被其他处理器捕获的运行时异常
     *
     * @param e 运行时异常
     * @return 响应实体，返回500错误码
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<?>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return ResponseEntity.status(500)
                .body(Result.error(500, "系统异常，请稍后重试"));
    }
}
