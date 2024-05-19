package org.example.couponapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.couponapi.controller.dto.CouponIssueRequestDto;
import org.example.couponapi.controller.dto.CouponIssueResponseDto;
import org.example.couponapi.service.CouponIssueRequestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CouponIssueController {

    private final CouponIssueRequestService couponIssueRequestService;

    // 쿠폰 발급 요청
    @PostMapping("/v1/issue")
    public CouponIssueResponseDto issueV1(@RequestBody CouponIssueRequestDto requestDto) {
        couponIssueRequestService.issueRequestV1(requestDto);
        return new CouponIssueResponseDto(true, null);
    }

    // 쿠폰 발급 요청
    @PostMapping("/v1/issue-async")
    public CouponIssueResponseDto asyncIssueV1(@RequestBody CouponIssueRequestDto requestDto) {
        couponIssueRequestService.asyncIssueRequestV1(requestDto);
        return new CouponIssueResponseDto(true, null);
    }

}
