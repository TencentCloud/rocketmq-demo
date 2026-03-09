package com.rocketmq.dashboard.util;

import com.rocketmq.dashboard.common.ResponseCode;
import com.rocketmq.dashboard.common.Result;

public class ResponseUtil {

    private ResponseUtil() {
    }

    public static <T> Result<T> success() {
        return Result.success();
    }

    public static <T> Result<T> success(T data) {
        return Result.success(data);
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.success(message, data);
    }

    public static <T> Result<T> error(ResponseCode responseCode) {
        return Result.error(responseCode);
    }

    public static <T> Result<T> error(ResponseCode responseCode, String message) {
        return Result.error(responseCode, message);
    }

    public static <T> Result<T> error(String message) {
        return Result.error(ResponseCode.INTERNAL_ERROR, message);
    }

    public static <T> Result<T> badRequest(String message) {
        return Result.error(ResponseCode.BAD_REQUEST, message);
    }

    public static <T> Result<T> notFound(String message) {
        return Result.error(ResponseCode.NOT_FOUND, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return Result.error(ResponseCode.UNAUTHORIZED, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return Result.error(ResponseCode.FORBIDDEN, message);
    }
}
