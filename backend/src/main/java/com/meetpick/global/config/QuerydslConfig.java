package com.meetpick.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [COMMON BACKBONE / MAIN CONSUMER B]
 * B가 Meeting 목록 동적 조회를 구현할 때 Repository에서 주입받아 사용하는 QueryDSL factory입니다.
 * EntityManager lifecycle은 Spring/JPA가 관리하므로 직접 close하지 않습니다.
 */
@Configuration
public class QuerydslConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
