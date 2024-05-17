package org.example.couponcore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issueQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart;

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd;

    // 발급 가능한 수량이 남아있는지 확인한다.
    public boolean availableIssueQuantity() {
        // 발급 수량에 제한이 없는 경우
        if (totalQuantity == null) {
            return true;
        }
        return totalQuantity > issueQuantity;
    }

    // 현재 시간이 쿠폰 발급 시작 날짜 이후이면서 동시에 발급 종료 날짜 이전인 경우에만 true를 반환해.
    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        // 쿠폰 발급 시작기간이 지금 이전인가? && 쿠폰 발급 종료기간이 지금 이후인가?
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    // 발급된 수량을 증가시킨다.
    public void issue() {
        if (!availableIssueQuantity()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY,
                    "발급 가능한 수량이 남아있지 않습니다. total : %s, issue : %s".formatted(totalQuantity, issueQuantity));
        }
        if (!availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE,
                    "쿠폰 발급 기간이 아닙니다. request : %s, issuedStart : %s, issueEnd: %s".formatted(LocalDateTime.now(), dateIssueStart, dateIssueEnd));
        }
        issueQuantity++;
    }

}
