package com.paolua.tomatoclockbackend.common.exception;

import com.paolua.tomatoclockbackend.common.response.Result;
import com.paolua.tomatoclockbackend.common.response.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GlobalExceptionHandler 单元测试
 * 测试全局异常处理器
 */
@DisplayName("GlobalExceptionHandler 单元测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("测试业务异常处理")
    void test_handleBusinessException() {
        // Given
        BusinessException businessException = new BusinessException(ResultCode.TASK_NOT_FOUND);

        // When
        ResponseEntity<Result<?>> response = exceptionHandler.handleBusinessException(businessException);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ResultCode.TASK_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals(ResultCode.TASK_NOT_FOUND.getMessage(), response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("测试系统异常处理")
    void test_handleException() {
        // Given
        Exception exception = new Exception("系统发生错误");

        // When
        ResponseEntity<Result<?>> response = exceptionHandler.handleException(exception);

        // Then
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
        assertEquals("系统异常，请稍后重试", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    @DisplayName("测试参数校验异常处理 - 单个字段错误")
    void test_handleMethodArgumentNotValidException_singleField() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("taskCreateRequest", "name", "任务名称不能为空");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        // When
        ResponseEntity<Result<?>> response = exceptionHandler.handleValidationException(exception);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertEquals("任务名称不能为空", response.getBody().getMessage());
    }

    @Test
    @DisplayName("测试参数校验异常处理 - 多个字段错误")
    void test_handleMethodArgumentNotValidException_multipleFields() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("taskCreateRequest", "name", "任务名称不能为空");
        FieldError fieldError2 = new FieldError("taskCreateRequest", "tomatoMinutes", "番茄时长不能为空");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<Result<?>> response = exceptionHandler.handleValidationException(exception);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("任务名称不能为空"));
        assertTrue(response.getBody().getMessage().contains("番茄时长不能为空"));
    }

    @Test
    @DisplayName("测试HTTP方法不支持异常处理")
    void test_handleHttpRequestMethodNotSupported() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/tasks");
        request.setMethod("POST");

        // When
        ResponseEntity<Result<?>> response = exceptionHandler.handleMethodNotSupported(
                new org.springframework.web.HttpRequestMethodNotSupportedException("GET"),
                request
        );

        // Then
        assertNotNull(response);
        assertEquals(405, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(405, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("不支持的请求方法"));
    }

    @Test
    @DisplayName("测试响应结构 - 包含时间戳")
    void test_response_structure() {
        // Given
        BusinessException businessException = new BusinessException(ResultCode.USER_NOT_FOUND);

        // When
        ResponseEntity<Result<?>> response = exceptionHandler.handleBusinessException(businessException);

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("测试自定义消息业务异常")
    void test_handleBusinessException_customMessage() {
        // Given
        String customMessage = "自定义业务错误消息";
        BusinessException businessException = new BusinessException(customMessage);

        // When
        ResponseEntity<Result<?>> response = exceptionHandler.handleBusinessException(businessException);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals(400, response.getBody().getCode());
        assertEquals(customMessage, response.getBody().getMessage());
    }

    @Test
    @DisplayName("测试带原因的业务异常")
    void test_handleBusinessException_withCause() {
        // Given
        Throwable cause = new RuntimeException("底层异常");
        BusinessException businessException = new BusinessException("业务异常", cause);

        // When
        ResponseEntity<Result<?>> response = exceptionHandler.handleBusinessException(businessException);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("业务异常", response.getBody().getMessage());
    }
}
