package com.paolua.tomatoclockbackend.common.exception;

import com.paolua.tomatoclockbackend.common.response.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务异常类
 * 用于抛出业务逻辑错误
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 使用ResultCode构造异常
     * @param resultCode 结果码枚举
     */
    public BusinessException(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 使用自定义消息构造异常（默认400错误码）
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this.code = 400;
        this.message = message;
    }

    /**
     * 使用自定义错误码和消息构造异常
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 使用ResultCode和 cause 构造异常
     * @param resultCode 结果码枚举
     * @param cause 原始异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 使用自定义消息和 cause 构造异常
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(cause);
        this.code = 400;
        this.message = message;
    }
}
