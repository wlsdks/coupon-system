package org.example.couponcore.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * ZSet에 값 추가
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
     * @param key
     * @param value
     * @return
     */
    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * Set의 크기 조회
     * @param key
     * @return
     */
    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * Set에 값이 존재하는지 확인
     * @param key
     * @param value
     * @return
     */
    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 쿠폰 발급 대기열 큐
     * @param key
     * @param value
     * @return
     */
    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

}
