package org.example.couponconsumer.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.couponcore.repository.redis.RedisRepository;
import org.example.couponcore.repository.redis.dto.CouponIssueRequest;
import org.example.couponcore.service.CouponIssueService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@Slf4j
@RequiredArgsConstructor
@EnableScheduling
@Component
public class CouponIssueListener {

    private final RedisRepository redisRepository;
    // 쿠폰 발급 트랜잭션을 처리하는 서비스
    private final CouponIssueService couponIssueService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String IssueRequestQueueKey = getIssueRequestQueueKey();

    /**
     * 주기적으로 queue에서 쿠폰 발급 대상을 확인하고 발급한다.
     *
     * @throws JsonProcessingException
     */
    @Scheduled(fixedDelay = 1000L)
    public void issue() throws JsonProcessingException {
        log.info("listen ...");
        while (existCouponIssueTarget()) {
            CouponIssueRequest target = getIssueTarget();
            log.info("발급 시작 target: %s".formatted(target));
            couponIssueService.issue(target.couponId(), target.userId());
            log.info("발급 완료 target: %s".formatted(target));
            removeIssueTarget();
        }
    }

    /**
     * queue에서 쿠폰 발급 대상이 있는지 확인
     *
     * @return
     */
    private boolean existCouponIssueTarget() {
        // queue에 쿠폰을 발급해야 할 대상(요청)이 있는지 확인
        return redisRepository.lSize(IssueRequestQueueKey) > 0;
    }

    /**
     * queue에서 쿠폰 발급 대상을 가져온다.
     *
     * @return
     */
    private CouponIssueRequest getIssueTarget() throws JsonProcessingException {
        return objectMapper.readValue(
                redisRepository.lIndex(IssueRequestQueueKey, 0),
                CouponIssueRequest.class
        );
    }

    /**
     * queue에서 쿠폰 발급 대상을 제거한다.
     */
    private void removeIssueTarget() {
        redisRepository.lPop(IssueRequestQueueKey);
    }

}
