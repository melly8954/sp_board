package com.equip.sp_board.common.config;

import com.equip.sp_board.common.trace.RequestTraceIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,"/api/v1/members", "/api/v1/auth/login").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<RequestTraceIdFilter> traceFilter() {
        FilterRegistrationBean<RequestTraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        // 실제 필터 인스턴스 지정
        registrationBean.setFilter(new RequestTraceIdFilter());

        // 필터 체인 내 실행 순서 지정 (낮을수록 먼저 실행)
        registrationBean.setOrder(1); // Trace ID는 모든 요청 전에 생성되어야 하므로 가장 먼저 실행

        return registrationBean; // Spring 필터를 등록하도록 반환
    }
}
