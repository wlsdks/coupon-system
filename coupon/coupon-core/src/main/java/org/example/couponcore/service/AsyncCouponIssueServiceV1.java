package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.repository.redis.RedisRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;

    /**
     * 비동기로 쿠폰 발급 요청
     * @param couponId
     * @param userId
     */
    public void issue(long couponId, long userId) {
        // 1. 유저의 요청을 Sorted set에 적재
        String key = "issue.request.sorted_set.couponId=%s".formatted(couponId);
        redisRepository.zAdd(key, String.valueOf(userId), System.currentTimeMillis());

        // 2. 유저의 요청의 순서를 조회


        // 3. 조회 결과를 선착순 조건과 비교

        // 4. 쿠폰 발급 Queue에 적재
    }

}
