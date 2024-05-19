package org.example.couponcore.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * String 값 저장
     * @param key
     * @param value
     */
    public Boolean zAdd(String key, String value, double score) {
        // 그냥 add를 사용하면 score가 업데이트 되어 중복 데이터의 선착순 순서가 바껴버리니 addIfAbsent를 사용해야 한다.
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }

}
