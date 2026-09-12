package com.meetpick.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * [COMMON SKELETON / OWNER A]
 * 현재는 JWT Filter 구현 전이므로 API를 임시 permitAll로 둔 백본 설정입니다.
 *
 * <p>A의 인증 PR 완료 조건:
 * - signup/login/reissue/invite preview 등 필요한 public endpoint만 permitAll
 * - 나머지는 authenticated
 * - JWT Filter를 SecurityFilterChain에 등록
 * - Refresh Cookie를 선택하면 CSRF/SameSite 정책을 ADR 근거와 함께 반영
 *
 * <p>B/C는 자기 기능 때문에 이 파일에 개별 endpoint를 무분별하게 추가하지 않고 A에게 요청합니다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                // Backbone 단계 임시 설정. A가 Refresh Token을 HttpOnly Cookie로 선택하면
                // CSRF 위협/방어(SameSite, CSRF token 등)를 ADR-001 실험 결과에 따라 재검토한다.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Backbone 단계: 아직 JWT Filter가 없으므로 임시 permitAll.
                        // A의 인증 PR에서 public API만 permitAll, 나머지는 authenticated로 교체한다.
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${meetpick.cors.allowed-origins:http://localhost:5173}") String allowedOriginsProperty
    ) {
        List<String> allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
