package com.melly.sp_board.common.config;

import com.melly.sp_board.auth.jwt.JwtFilter;
import com.melly.sp_board.auth.jwt.JwtProvider;
import com.melly.sp_board.auth.security.CustomAuthenticationProvider;
import com.melly.sp_board.common.trace.RequestTraceIdFilter;
import com.melly.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestTraceIdFilter requestTraceIdFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,"/api/v1/members", "/api/v1/auth/login").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(requestTraceIdFilter, UsernamePasswordAuthenticationFilter.class) // Trace ID 먼저
                .addFilterBefore(new JwtFilter(jwtProvider, memberRepository), UsernamePasswordAuthenticationFilter.class); // JWT 인증
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 미등록 시 기본 AuthenticationProvider 실행
    @Bean
    public AuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(memberRepository, passwordEncoder);
    }
}
