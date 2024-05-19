package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV2 {

    private final RedisRepository redisRepository;
    private final CouponCacheService couponCacheService;

    /**
     * 비동기로 쿠폰 발급 요청
     *
     * @param couponId
     * @param userId
     */
    public void issue(long couponId, long userId) {
        CouponRedisEntity coupon = couponCacheService.getCouponCache(couponId);
        coupon.checkIssuableCoupon();
        issueRequest(couponId, userId, coupon.totalQuantity());
    }

    /**
     * 쿠폰 발급 요청 (redis Script)
     *
     * @param couponId
     * @param userId
     */
    private void issueRequest(long couponId, long userId, Integer totalIssueQuantity) {
        if (totalIssueQuantity == null) {
            redisRepository.issueRequest(couponId, userId, Integer.MAX_VALUE); // MAX_VALUE로 검증 우회
        }
        redisRepository.issueRequest(couponId, userId, totalIssueQuantity);
    }

}
