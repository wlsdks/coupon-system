package org.example.couponapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponapi.controller.dto.CouponIssueRequestDto;
import org.example.couponcore.component.DistributeLockExecutor;
import org.example.couponcore.service.AsyncCouponIssueServiceV1;
import org.example.couponcore.service.AsyncCouponIssueServiceV2;
import org.example.couponcore.service.CouponIssueService;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;
    private final AsyncCouponIssueServiceV1 asyncCouponIssueServiceV1;
    private final DistributeLockExecutor distributeLockExecutor;
    private final AsyncCouponIssueServiceV2 asyncCouponIssueServiceV2;

    public void issueRequestV1(CouponIssueRequestDto requestDto) {
        // 분산락 처리
//        distributeLockExecutor.execute("lock_" + requestDto.couponId(), 10000, 10000, () -> {
            couponIssueService.issue(requestDto.couponId(), requestDto.userId());
//        });
        log.info("쿠폰 발급 완료. couponId: %s, userId: %s".formatted(requestDto.couponId(), requestDto.userId()));
    }

    /**
     * 비동기로 쿠폰 발급 요청 v1 (redis)
     * @param requestDto
     */
    public void asyncIssueRequestV1(CouponIssueRequestDto requestDto) {
        asyncCouponIssueServiceV1.issue(requestDto.couponId(), requestDto.userId());
    }

    /**
     * 비동기로 쿠폰 발급 요청 v2 (redis script)
     * @param requestDto
     */
    public void asyncIssueRequestV2(CouponIssueRequestDto requestDto) {
        asyncCouponIssueServiceV2.issue(requestDto.couponId(), requestDto.userId());
    }

}
