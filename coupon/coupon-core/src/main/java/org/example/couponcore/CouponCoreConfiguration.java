package org.example.couponcore;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Spring AOP 프록시를 활성화하고, 프록시 객체를 노출시키기 위해 설정
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@EnableJpaAuditing
@ComponentScan
@EnableAutoConfiguration
public class CouponCoreConfiguration {
}
