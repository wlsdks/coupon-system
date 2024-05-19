package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.stereotype.Service;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {

    private final RedisRepository redisRepository;

    /**
     * 쿠폰 발급 가능 여부 확인 (전체)
     * @param couponRedisEntity
     * @param userId
     */
    public void checkCouponIssueQuantity(CouponRedisEntity couponRedisEntity, long userId) {
        if (!availableTotalIssueQuantity(couponRedisEntity.totalQuantity(), couponRedisEntity.id())) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY,
                    "발급 가능한 수량을 초과하였습니다. couponId: %s, userId: %s".formatted(couponRedisEntity.id(), userId));
        }
        if (!availableUserIssueQuantity(couponRedisEntity.id(), userId)) {
            throw new CouponIssueException(ErrorCode.DUPLICATE_COUPON_ISSUE,
                    "이미 발급된 쿠폰입니다. couponId: %s, userId: %s".formatted(couponRedisEntity.id(), userId));
        }
    }

    /**
     * 쿠폰 발급 가능 여부 확인 (총 발급 수량 체크)
     * @param totalQuantity
     * @param couponId
     * @return
     */
    public boolean availableTotalIssueQuantity(Integer totalQuantity, long couponId) {
        // 총 발급 수량이 null이라면 무제한
        if (totalQuantity == null) {
            return true;
        }
        String key = getIssueRequestKey(couponId);
        // 총 발급 수량이 지금까지 발급된 수량(set 크기)보다 크다면 아직 발급 가능
        return totalQuantity > redisRepository.sCard(key);
    }

    /**
     * 쿠폰 발급 가능 여부 확인 (중복 체크)
     * @param couponId
     * @param userId
     * @return
     */
    public boolean availableUserIssueQuantity(long couponId, long userId) {
        // 쿠폰 발급 제한 수량을 확인하기 위한 key 생성
        String key = getIssueRequestKey(couponId);
        // set에 존재하지 않는다면 발급 가능
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }

}
