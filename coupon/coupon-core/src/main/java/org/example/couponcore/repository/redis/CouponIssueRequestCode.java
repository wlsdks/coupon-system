package org.example.couponcore.repository.redis;

import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;

/**
 * Redis Script 결과에 대한 코드다.
 * 레디스 스크립트 메서드의 return값이 1,2,3 이므로 이를 enum으로 관리한다.
 * issueRequest() 메서드에서 반환값을 code로 받아서 처리한다.
 *
 * @see RedisRepository#issueRequestScript()
 * @see RedisRepository#issueRequest(long, long, int)
 */
public enum CouponIssueRequestCode {
    SUCCESS(1),
    DUPLICATED_COUPON_ISSUE(2),
    INVALID_COUPON_ISSUE_QUANTITY(3);

    CouponIssueRequestCode(int code) {

    }

    /**
     * 코드를 찾는다.
     * @param code
     * @return
     */
    public static CouponIssueRequestCode find(String code) {
        int codeValue = Integer.parseInt(code);
        return switch (codeValue) {
            case 1 -> SUCCESS;
            case 2 -> DUPLICATED_COUPON_ISSUE;
            case 3 -> INVALID_COUPON_ISSUE_QUANTITY;
            default -> throw new IllegalArgumentException("존재하지 않는 코드입니다. %s".formatted(code));
        };
    }

    /**
     * 요청 결과를 확인한다.
     * @param code
     */
    public static void checkRequestResult(CouponIssueRequestCode code) {
        if (code == INVALID_COUPON_ISSUE_QUANTITY) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과하였습니다.");
        }
        if (code == DUPLICATED_COUPON_ISSUE) {
            throw new CouponIssueException(ErrorCode.DUPLICATE_COUPON_ISSUE, "이미 발급된 쿠폰입니다.");
        }
    }
}
