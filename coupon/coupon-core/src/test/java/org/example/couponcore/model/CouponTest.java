package org.example.couponcore.model;

import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class CouponTest {

    @DisplayName("발급 수량이 남아있다면 true를 반환한다.")
    @Test
    void availableIssueQuantity_1() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100) // 전체 100개
                .issuedQuantity(99)  // 발급된 쿠폰이 99개
                .build();

        //when
        boolean result = coupon.availableIssueQuantity();

        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("발급 수량이 소진되었다면 false를 반환한다.")
    @Test
    void availableIssueQuantity_2() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100) // 전체 100개
                .issuedQuantity(100) // 발급된 쿠폰이 100개
                .build();

        //when
        boolean result = coupon.availableIssueQuantity();

        //then
        Assertions.assertFalse(result);
    }

    @DisplayName("최대 발급 수량이 설정되지 않았다면 true를 반환한다.")
    @Test
    void availableIssueQuantity_3() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null) // 전체 100개
                .issuedQuantity(100) // 발급된 쿠폰이 100개
                .build();

        //when
        boolean result = coupon.availableIssueQuantity();

        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("쿠폰 발급 기간이 시작되지 않았다면 false를 반환한다.")
    @Test
    void availableIssueDate_1() {
        //given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1L)) // 1일 후
                .dateIssueEnd(LocalDateTime.now().plusDays(2L))   // 2일 후
                .build();

        //when
        boolean result = coupon.availableIssueDate();

        //then
        Assertions.assertFalse(result);
    }

    @DisplayName("쿠폰 발급 기간에 해당되면 true를 반환한다.")
    @Test
    void availableIssueDate_2() {
        //given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1L)) // 1일 전
                .dateIssueEnd(LocalDateTime.now().plusDays(2L))    // 2일 후
                .build();

        //when
        boolean result = coupon.availableIssueDate();

        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("쿠폰 발급 기간이 종료된 상황이면 false를 반환한다.")
    @Test
    void availableIssueDate_3() {
        //given
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2L)) // 2일 전
                .dateIssueEnd(LocalDateTime.now().minusDays(1L))   // 1일 전
                .build();

        //when
        boolean result = coupon.availableIssueDate();

        //then
        Assertions.assertFalse(result);
    }

    @DisplayName("발급 수량과 발급 기간이 유효하다면 발급에 성공한다.")
    @Test
    void issue_1() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when
        coupon.issue();

        //then
        Assertions.assertEquals(coupon.getIssuedQuantity(), 100);
    }

    @DisplayName("발급 수량을 초과하면 예외를 반환한다.")
    @Test
    void issue_2() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when & then
        var exception = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @DisplayName("발급 기간이 아니면 예외를 반환한다.")
    @Test
    void issue_3() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when
        var exception = Assertions.assertThrows(CouponIssueException.class, coupon::issue);
        Assertions.assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @DisplayName("발급 기한이 종료되면 true를 반환한다.")
    @Test
    void isIssueComplete_1() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        //when
        boolean result = coupon.isIssueComplete();

        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("잔여 발급 수량이 없다면 true를 반환한다.")
    @Test
    void isIssueComplete_2() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when
        boolean result = coupon.isIssueComplete();

        //then
        Assertions.assertTrue(result);
    }


    @DisplayName("잔여 기간과 수량이 유효하면 false를 반환한다.")
    @Test
    void isIssueComplete_3() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(3))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when
        boolean result = coupon.isIssueComplete();

        //then
        Assertions.assertFalse(result);
    }

}