package com.rhy.interviewprep.common;

public enum ErrorCode {
    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请登录"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    INTERNAL_ERROR(500, "服务器内部错误"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_EXISTS(1003, "用户名已存在"),
    USER_NOT_LOGIN(1004, "用户未登录"),
    USER_TOKEN_EXPIRED(1005, "登录已过期，请重新登录"),
    USER_TOKEN_INVALID(1006, "无效的登录凭证"),

    JD_NOT_FOUND(2001, "JD记录不存在"),
    JD_PARSE_ERROR(2002, "JD解析失败"),

    SKILL_NOT_FOUND(3001, "技能不存在"),

    MATCH_ANALYSIS_NOT_FOUND(4001, "匹配分析记录不存在"),

    LEARNING_PLAN_NOT_FOUND(5001, "学习计划不存在"),

    QUESTION_NOT_FOUND(6001, "题目不存在"),

    AI_SERVICE_ERROR(7001, "AI服务调用失败"),
    AI_API_KEY_INVALID(7002, "AI API Key无效"),

    DATABASE_ERROR(8001, "数据库操作失败"),
    DATA_VALIDATION_ERROR(8002, "数据校验失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
