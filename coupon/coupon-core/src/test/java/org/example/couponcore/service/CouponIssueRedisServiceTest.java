package org.example.couponcore.service;

import org.example.couponcore.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.stream.IntStream;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.junit.jupiter.api.Assertions.*;

class CouponIssueRedisServiceTest extends TestConfig {

    @Autowired
    CouponIssueRedisService sut;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void clear() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        assert redisKeys != null;
        redisTemplate.delete(redisKeys);
    }

    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 존재하면 true를 반환한다.")
    @Test
    void availableTotalIssueQuantity_1() {
        //given
        int totalIssueQuantity = 10;
        long couponId = 1L;

        //when
        boolean result = sut.availableTotalIssueQuantity(totalIssueQuantity, couponId);

        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("쿠폰 수량 검증 - 발급 가능 수량이 모두 소진되면 false를 반환한다.")
    @Test
    void availableTotalIssueQuantity_2() {
        //given
        int totalIssueQuantity = 10;
        long couponId = 1L;
        IntStream.range(0, totalIssueQuantity).forEach(i -> {
            redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(i));
        });

        //when
        boolean result = sut.availableTotalIssueQuantity(totalIssueQuantity, couponId);

        //then
        Assertions.assertFalse(result);
    }

    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하지 않으면 True를 반환한다.")
    @Test
    void availableUserIssueQuantity_1() {
        //given
        long couponId = 1L;
        long userId = 1L;

        //when
        boolean result = sut.availableUserIssueQuantity(couponId, userId);

        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("쿠폰 중복 발급 검증 - 발급된 내역에 유저가 존재하면 False를 반환한다.")
    @Test
    void availableUserIssueQuantity_2() {
        //given
        long couponId = 1L;
        long userId = 1L;
        redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));

        //when
        boolean result = sut.availableUserIssueQuantity(couponId, userId);

        //then
        Assertions.assertFalse(result);
    }


}