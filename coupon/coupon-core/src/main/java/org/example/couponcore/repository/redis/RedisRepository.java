package org.example.couponcore.repository.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.couponcore.exception.CouponIssueException;
import org.example.couponcore.exception.ErrorCode;
import org.example.couponcore.repository.redis.dto.CouponIssueRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

@RequiredArgsConstructor
@Repository
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> issueScript = issueRequestScript();
    private final String IssueRequestQueueKey = getIssueRequestQueueKey();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * ZSet에 값 추가
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public Boolean zAdd(String key, String value, double score) {
        // 그냥 add를 사용하면 score가 업데이트 되어 중복 데이터의 선착순 순서가 바껴버리니 addIfAbsent를 사용해야 한다.
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

    /**
     * Set에 값 추가
     *
     * @param key
     * @param value
     * @return
     */
    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * Set의 크기 조회
     *
     * @param key
     * @return
     */
    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * Set에 값이 존재하는지 확인
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 쿠폰 발급 대기열 큐
     *
     * @param key
     * @param value
     * @return
     */
    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }


    /**
     * 쿠폰 발급 대기열 큐의 사이즈 확인
     *
     * @param key
     * @return
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 쿠폰 발급 대기열 큐에서 값 가져오기
     *
     * @param key
     * @param index
     * @return
     */
    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 쿠폰 발급 대기열 큐에서 값 제거
     *
     * @param key
     * @return
     */
    public String lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * Redis Script를 사용하는 쿠폰 발급 요청 (동시성 제어)
     *
     * @param couponId
     * @param userId
     */
    public void issueRequest(long couponId, long userId, int totalIssueQuantity) {
        String issueRequestKey = getIssueRequestKey(couponId);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);
        try {
            // 레디스 Script를 사용하여 쿠폰 발급 요청을 Redis에 저장하고 Redis의 List 큐에 넣는다.
            String code = redisTemplate.execute(
                    issueScript,                                        // SCRIPT
                    List.of(issueRequestKey, IssueRequestQueueKey),     // KEYS[1], KEYS[2]
                    String.valueOf(userId),                             // ARGV[1]
                    String.valueOf(totalIssueQuantity),                 // ARGV[2]
                    objectMapper.writeValueAsString(couponIssueRequest) // ARGV[3]
            );
            // 쿠폰 발급 요청 결과 확인 (유효성 검사)
            CouponIssueRequestCode.checkRequestResult(CouponIssueRequestCode.find(code));
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUEST, "input: %s".formatted(couponIssueRequest));
        }
    }

    /**
     * Redis 쿠폰 발급 요청 스크립트
     *
     * @return
     */
    private RedisScript<String> issueRequestScript() {
        String script = """
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then 
                    return '2'
                end
                                
                if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                    redis.call('SADD', KEYS[1], ARGV[1])
                    redis.call('RPUSH', KEYS[2], ARGV[3])
                    return '1'
                end
                                
                return '3'
                """;
        return RedisScript.of(script, String.class);
    }

}
