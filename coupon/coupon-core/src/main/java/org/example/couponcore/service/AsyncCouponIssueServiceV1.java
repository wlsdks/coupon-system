package org.example.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.component.DistributeLockExecutor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponIssueRequest;
import org.springframework.stereotype.Service;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 동시성 제어를 위한 분산락
    private final DistributeLockExecutor distributeLockExecutor;

    /**
     * 비동기로 쿠폰 발급 요청
     *
     * @param couponId
     * @param userId
     */
    public void issue(long couponId, long userId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        // 쿠폰 발급 가능 여부 확인
        if (!coupon.availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE,
                    "발급 가능한 날짜가 아닙니다. couponId: %s, userId: %s".formatted(couponId, userId));
        }
        // 분산락 처리 (동시성 제어)
        distributeLockExecutor.execute("lock %s".formatted(couponId), 3000, 3000, () -> {
            if (!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
                throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY,
                        "발급 가능한 수량을 초과하였습니다. couponId: %s, userId: %s".formatted(couponId, userId));
            }
            if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(ErrorCode.DUPLICATE_COUPON_ISSUE,
                        "이미 발급된 쿠폰입니다. couponId: %s, userId: %s".formatted(couponId, userId));
            }
            issueRequest(couponId, userId);
        });
    }

    /**
     * 쿠폰 발급 요청
     *
     * @param couponId
     * @param userId
     */
    private void issueRequest(long couponId, long userId) {
        // 쿠폰 발급 요청 객체 생성
        CouponIssueRequest issueRequest = new CouponIssueRequest(couponId, userId);
        try {
            // 쿠폰 발급 요청을 Redis에 저장하고 Redis의 List 큐에 넣는다.
            String value = objectMapper.writeValueAsString(issueRequest);
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(issueRequest));
        }
    }

}
