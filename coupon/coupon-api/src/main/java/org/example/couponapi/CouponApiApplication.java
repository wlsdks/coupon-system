package org.example.couponapi;

import org.example.couponcore.CouponCoreConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(CouponCoreConfiguration.class)
@SpringBootApplication
public class CouponApiApplication {

    public static void main(String[] args) {

        /**
         * 명시적으로 application-core.yml과 application-api.yml 설정을 불러와 합친다는 의미다.
         * 이렇게 설정하면, application-core.yml에 있는 데이터베이스 설정과 같은 항목들이 CouponApiApplication에서도 사용될 수 있다.
         */
        System.setProperty("spring.config.name", "application-core, application-api");
        SpringApplication.run(CouponApiApplication.class, args);
    }

}
