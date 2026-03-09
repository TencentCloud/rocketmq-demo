package com.rocketmq.dashboard.common;

public enum ResponseCode {
    SUCCESS(200, "Success"),
    CREATED(201, "Created"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    
    CONFIG_ERROR(1001, "Configuration Error"),
    TENCENT_CLOUD_ERROR(1002, "Tencent Cloud API Error"),
    VALIDATION_ERROR(1003, "Validation Error"),
    BUSINESS_ERROR(1004, "Business Error");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
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
