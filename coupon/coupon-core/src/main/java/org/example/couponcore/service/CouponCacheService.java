package org.example.couponcore.service;

import lombok.RequiredArgsConstructor;
import org.example.couponcore.model.Coupon;
import org.example.couponcore.repository.redis.dto.CouponRedisEntity;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {

    private final CouponIssueService couponIssueService;

    /**
     * 쿠폰 캐시 조회 (redis 캐시)
     * @param couponId
     * @return
     */
    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }

    /**
     * 쿠폰 캐시 조회 (로컬 캐시)
     * @param couponId
     * @return
     */
    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(long couponId) {
        // 로컬 캐시에 있으면 먼저 조회하고 없으면 redis 캐시 조회
        return proxy().getCouponCache(couponId);
    }

    /**
     * @Cacheable 어노테이션은 이러한 Aspect 중 하나로, 메서드의 결과를 캐시에 저장하고,같은 인자로 메서드가 호출될 때 캐시에서 결과를 가져오는 로직을 추가합니다.
     * 이 로직은 프록시 객체를 통해 동작하므로, 클래스가 this를 사용해서 자기 자신의 메서드를 직접 호출하면 프록시를 거치지 않게 되어 캐싱 로직이 동작하지 않게 됩니다.
     * 따라서, proxy().getCouponCache(couponId);와 같이 프록시 객체를 통해 메서드를 호출함으로써 캐싱 로직이 정상적으로 동작하도록 합니다.
     * 이렇게 하면 getCouponCache(couponId) 메서드의 결과가 캐시에 저장되고, 같은 couponId로 메서드가 다시 호출되면 캐시에서 결과를 가져옵니다.
     */
    private CouponCacheService proxy() {
        return ((CouponCacheService) AopContext.currentProxy());
    }

}
