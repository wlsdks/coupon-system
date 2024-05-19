package org.example.couponcore.repository.redis.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.model.CouponType;

import java.time.LocalDateTime;

/**
 * 쿠폰 Redis 캐싱 Entity
 */
public record CouponRedisEntity(
        Long id,
        CouponType couponType,
        Integer totalQuantity,

        // jackson 라이브러리 추가가 필요함
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueStart,

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        LocalDateTime dateIssueEnd
) {

    // 쿠폰 엔티티를 받아서 Redis 캐싱 엔티티로 변환
    public CouponRedisEntity(Coupon coupon) {
        this(
                coupon.getId(),
                coupon.getCouponType(),
                coupon.getTotalQuantity(),
                coupon.getDateIssueStart(),
                coupon.getDateIssueEnd()
        );
    }

    // 쿠폰 발급 가능 여부 확인
    private boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    // 쿠폰 발급 가능 여부 확인 public
    public void checkIssuableCoupon() {
        if (!availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 날짜가 아닙니다. couponId: %s, dateIssueStart: %s, dateIssueEnd: %s".formatted(id, dateIssueStart, dateIssueEnd));
        }
    }

}
