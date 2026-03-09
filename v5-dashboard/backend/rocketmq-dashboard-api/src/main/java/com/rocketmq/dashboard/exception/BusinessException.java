package com.rocketmq.dashboard.exception;

import com.rocketmq.dashboard.common.ResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String message;

    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }

    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
        this.message = message;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResponseCode.BUSINESS_ERROR.getCode();
        this.message = message;
    }
}
