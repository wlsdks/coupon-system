package org.example.couponcore.exception;

import lombok.Getter;

@Getter
public class CouponIssueException extends RuntimeException {

    private final ErrorCode errorCode;

    private final String message;

    public CouponIssueException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    // 메시지는 오버라이드 한다.
    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, message);
    }

}
