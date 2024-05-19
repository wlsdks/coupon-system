package org.example.couponcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.couponcore.TestConfig;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.model.CouponType;
import org.example.couponcore.repository.mysql.CouponJpaRepository;
import org.example.couponcore.repository.redis.dto.CouponIssueRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.IntStream;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;
import static org.junit.jupiter.api.Assertions.*;

class AsyncCouponIssueServiceV1Test extends TestConfig {

    @Autowired
    AsyncCouponIssueServiceV1 sut;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @BeforeEach
    void clear() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        assert redisKeys != null;
        redisTemplate.delete(redisKeys);
    }

    @DisplayName("쿠폰 발급 - 쿠폰이 존재하지 않는다면 예외를 반환한다.")
    @Test
    void issue_1() {
        //given
        long couponId = 1L;
        long userId = 1L;

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> sut.issue(couponId, userId));
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);
    }

    @DisplayName("쿠폰 발급 - 발급 가능한 수량이 존재하지 않는다면 예외를 반환한다.")
    @Test
    void issue_2() {
        //given
        long userId = 1000L;
        Coupon coupon = saveCoupon();
        saveRedisCouponData(coupon);

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @DisplayName("쿠폰 발급 - 이미 발급된 유저라면 예외를 반환한다.")
    @Test
    void issue_3() {
        //given
        long userId = 1L;
        Coupon coupon = saveCoupon();
        redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATE_COUPON_ISSUE);
    }

    @DisplayName("쿠폰 발급 - 발급 기한이 유효하지 않다면 예외를 반환한다.")
    @Test
    void issue_4() {
        //given
        long userId = 1L;
        Coupon coupon = invalidDateCoupon();
        redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> sut.issue(coupon.getId(), userId));
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @DisplayName("쿠폰 발급 - 쿠폰 발급을 정상적으로 기록한다.")
    @Test
    void issue_5() {
        //given
        long userId = 1L;
        Coupon coupon = saveCoupon();

        //when
        sut.issue(coupon.getId(), userId);

        // then
        Boolean isSaved = redisTemplate.opsForSet().isMember(getIssueRequestKey(coupon.getId()), String.valueOf(userId));
        assertTrue(isSaved);
    }

    @DisplayName("쿠폰 발급 - 쿠폰 발급 요청이 성공하면 쿠폰 발급 queue에 적재된다.")
    @Test
    void issue_6() throws JsonProcessingException {
        //given
        long userId = 1L;
        Coupon coupon = saveCoupon();
        CouponIssueRequest request = new CouponIssueRequest(coupon.getId(), userId);

        //when
        sut.issue(coupon.getId(), userId);

        // then
        String savedIssueRequest = redisTemplate.opsForList().leftPop(getIssueRequestQueueKey());
        Assertions.assertEquals(new ObjectMapper().writeValueAsString(request), savedIssueRequest);
    }



    private Coupon saveCoupon() {
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        return couponJpaRepository.save(coupon);
    }

    private Coupon invalidDateCoupon() {
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 테스트 쿠폰")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        return couponJpaRepository.save(coupon);
    }

    private void saveRedisCouponData(Coupon coupon) {
        IntStream.range(0, coupon.getTotalQuantity()).forEach(value -> {
            redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(value));
        });
    }



}