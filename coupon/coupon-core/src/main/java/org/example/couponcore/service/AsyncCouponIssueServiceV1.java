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
import org.example.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.stereotype.Service;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CouponCacheService couponCacheService;

    // 동시성 제어를 위한 분산락
    private final DistributeLockExecutor distributeLockExecutor;

    /**
     * 비동기로 쿠폰 발급 요청
     *
     * @param couponId
     * @param userId
     */
    public void issue(long couponId, long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();
        // 분산락 처리 (동시성 제어)
//        distributeLockExecutor.execute("lock %s".formatted(couponId), 3000, 3000, () -> {
            couponIssueRedisService.checkCouponIssueQuantity(coupon, userId);
            issueRequest(couponId, userId);
//        });
    }

    // todo: 아래의 과정을 하나로 묶어야 동시성 문제가 발생하지 않을 것이다.
    // 1. totalQuantity -> redisRepository.sCard(key)             // 쿠폰 발급 수량 제어
    // 2. !redisRepository.sIsMember(key, String.valueOf(userId)) // 중복 발급 제어
    // 3. redisRepository.sAdd                                    // 쿠폰 발급 요청 저장
    // 4. redisRepository.rPush                                   // 쿠폰 발급 요청 큐에 넣기

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
