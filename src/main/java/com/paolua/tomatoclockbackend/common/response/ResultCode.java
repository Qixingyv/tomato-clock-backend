package com.paolua.tomatoclockbackend.common.response;

/**
 * 响应码枚举
 * 定义API返回的状态码和对应的消息
 */
public enum ResultCode {

    // ========== 通用HTTP状态码 ==========
    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ========== 业务错误码 (1000-1999) ==========

    // 用户相关 (1000-1099)
    USER_NOT_FOUND(1000, "用户不存在"),
    USER_ALREADY_EXISTS(1001, "用户已存在"),
    USER_LOGIN_FAILED(1002, "登录失败"),
    USER_TOKEN_EXPIRED(1003, "Token已过期"),
    USER_TOKEN_INVALID(1004, "Token无效"),

    // 任务相关 (1100-1199)
    TASK_NOT_FOUND(1100, "任务不存在"),
    TASK_NAME_EMPTY(1101, "任务名称不能为空"),
    TASK_TYPE_INVALID(1102, "任务类型无效"),
    TASK_ALREADY_COMPLETED(1103, "任务已完成"),
    TASK_EXPIRED(1104, "任务已过期"),
    TASK_CANCELLED(1105, "任务已取消"),

    // 番茄钟相关 (1200-1299)
    TOMATO_NOT_FOUND(1200, "番茄记录不存在"),
    TOMATO_IN_PROGRESS(1201, "番茄钟正在进行中"),
    TOMATO_NOT_STARTED(1202, "番茄钟未开始"),

    // 统计相关 (1300-1399)
    STATISTICS_PERIOD_INVALID(1300, "统计周期无效"),
    STATISTICS_DATE_RANGE_INVALID(1301, "日期范围无效");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
