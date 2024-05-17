package org.example.couponcore.service;

import org.example.couponcore.TestConfig;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.model.CouponIssue;
import org.example.couponcore.model.CouponType;
import org.example.couponcore.repository.mysql.CouponIssueJpaRepository;
import org.example.couponcore.repository.mysql.CouponIssueRepository;
import org.example.couponcore.repository.mysql.CouponJpaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CouponIssueServiceTest extends TestConfig {

    @Autowired
    CouponIssueService sut;

    @Autowired
    CouponIssueJpaRepository couponIssueJpaRepository;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    // 매번 테스트 전에 데이터를 삭제한다.
    @BeforeEach
    void clean() {
        couponIssueJpaRepository.deleteAllInBatch();
        couponJpaRepository.deleteAllInBatch();
    }

    @DisplayName("쿠폰 발급 내역이 존재하면 예외를 반환한다.")
    @Test
    void saveCouponIssue_1() {
        //given
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();
        couponIssueJpaRepository.save(couponIssue);

        //when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, () -> {
            sut.saveCouponIssue(couponIssue.getCouponId(), couponIssue.getUserId());
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATE_COUPON_ISSUE);
    }

    @DisplayName("쿠폰 발급 내역이 존재하지 않는 경우 쿠폰이 발급된다.")
    @Test
    void saveCouponIssue_2() {
        //given
        long couponId = 1L;
        long userId = 1L;

        //when
        var result = sut.saveCouponIssue(couponId, userId);

        // then
        Assertions.assertTrue(couponIssueJpaRepository.findById(result.getId()).isPresent());
        Assertions.assertEquals(result.getCouponId(), couponId);
        Assertions.assertEquals(result.getUserId(), userId);
    }

    @DisplayName("발급 수량, 기한, 중복 발급 문제가 없다면 쿠폰이 발급된다.")
    @Test
    void issue_1() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(100)
                .issueQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        //when
        sut.issue(coupon.getId(), userId);

        //then
        var couponResult = couponJpaRepository.findById(coupon.getId()).get();
        Assertions.assertEquals(couponResult.getIssueQuantity(), 1);
        var issue = couponIssueRepository.findFirstCouponIssue(coupon.getId(), userId);
        Assertions.assertNotNull(issue);
    }

    @DisplayName("발급 수량에 문제가 있다면 예외를 반환한다.")
    @Test
    void issue_2() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(100)
                .issueQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        //when & then
        var exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @DisplayName("발급 기한에 문제가 있다면 예외를 반환한다.")
    @Test
    void issue_3() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(100)
                .issueQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        //when & then
        var exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId);
        });
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @DisplayName("중복 발급 검증에 문제가 있으면 예외를 반환한다.")
    @Test
    void issue_4() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰")
                .totalQuantity(100)
                .issueQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        couponJpaRepository.save(coupon);

        // 미리 이슈를 발급한다.
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .build();
        couponIssueJpaRepository.save(couponIssue);

        //when & then
        var exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(coupon.getId(), userId); // 이슈를 발급한다.
        });
        // 중복 발급 예외를 반환한다.
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATE_COUPON_ISSUE);
    }

    @DisplayName("쿠폰이 존재하지 않는다면 예외를 반환한다.")
    @Test
    void issue_5() {
        //given
        long userId = 1L;
        long couponId = 1L;

        //when & then
        var exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            sut.issue(couponId, userId);
        });
        // 중복 발급 예외를 반환한다.
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);
    }

}