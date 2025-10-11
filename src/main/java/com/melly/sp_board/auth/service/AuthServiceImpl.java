package com.melly.sp_board.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melly.sp_board.auth.dto.LoginRequest;
import com.melly.sp_board.auth.dto.LoginResponse;
import com.melly.sp_board.auth.dto.ReIssueTokenDto;
import com.melly.sp_board.auth.dto.RefreshTokenDto;
import com.melly.sp_board.auth.jwt.JwtProvider;
import com.melly.sp_board.auth.security.PrincipalDetails;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.common.util.CookieUtil;
import com.melly.sp_board.member.domain.Member;
import io.lettuce.core.RedisCommandTimeoutException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest dto, HttpServletResponse response) {
        try{
            // AuthenticationManager 는 위임자(Delegator)
            // AuthenticationProvider 가 실제 인증 로직 담당자(Worker), CustomAuthenticationProvider 에서 인증 실행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            Member member = ((PrincipalDetails) authentication.getPrincipal()).getMember();

            String tokenId = UUID.randomUUID().toString();

            String accessToken = jwtProvider.createJwt(tokenId, "AccessToken", member.getUsername(), member.getRole().name(),600000L);
            String refreshToken = jwtProvider.createJwt(tokenId,"RefreshToken", member.getUsername(), member.getRole().name(), 86400000L);

            RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                    .username(member.getUsername())
                    .role(member.getRole().name())
                    .tokenId(tokenId)
                    .issuedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(86400000L)))
                    .build();
            redisTemplate.opsForValue().set("RefreshToken:" + member.getUsername() + ":" + tokenId, refreshTokenDto, Duration.ofDays(1));

            // 쿠키 생성
            Cookie refreshCookie = cookieUtil.createCookie("RefreshToken", refreshToken);
            response.addCookie(refreshCookie);

            return LoginResponse.builder()
                    .memberId(member.getMemberId())
                    .username(member.getUsername())
                    .role(member.getRole().name())
                    .tokenId(tokenId)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (BadCredentialsException e) {
            throw new CustomException(ErrorType.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        } catch (DisabledException e) {
            throw new CustomException(ErrorType.UNAUTHORIZED, "탈퇴 처리된 계정입니다.");
        }
    }

    @Override
    @Transactional
    public ReIssueTokenDto reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refresh 토큰 추출
        String refreshToken = cookieUtil.getValue(request);
        if (refreshToken == null) {
            throw new CustomException(ErrorType.NOT_FOUND, "Refresh Token 이 요청에 존재하지 않습니다.");
        }

        // 카테고리 확인
        if (!"RefreshToken".equals(jwtProvider.getTokenType(refreshToken))) {
            throw new CustomException(ErrorType.UNAUTHORIZED, "유효하지 않은 Refresh Token 입니다.");
        }

        // 토큰 만료 확인
        if (jwtProvider.isExpired(refreshToken)) {
            throw new CustomException(ErrorType.UNAUTHORIZED, "만료된 Refresh Token 입니다.");
        }

        String username = jwtProvider.getUsername(refreshToken);
        String tokenId = jwtProvider.getTokenId(refreshToken);

        String key = "RefreshToken:" + username + ":" + tokenId;
        Object redisValue = redisTemplate.opsForValue().get(key);

        if (redisValue == null) {
            throw new CustomException(ErrorType.NOT_FOUND, "Refresh Token이 Redis에 존재하지 않습니다.");
        }

        // Object -> DTO 변환
        RefreshTokenDto refreshTokenDto = objectMapper.convertValue(redisValue, RefreshTokenDto.class);

        // 새로운 tokenId 생성
        String newTokenId = UUID.randomUUID().toString();

        // 새로운 accessToken, refreshToken 생성
        String newAccessToken = jwtProvider.createJwt(newTokenId, "AccessToken", username, refreshTokenDto.getRole(),600000L);
        String newRefreshToken = jwtProvider.createJwt(newTokenId,"RefreshToken", username, refreshTokenDto.getRole(), 86400000L);

        // Redis에 새로운 refreshToken 저장
        RefreshTokenDto newRefreshTokenDto = RefreshTokenDto.builder()
                .username(username)
                .role(refreshTokenDto.getRole())
                .tokenId(newTokenId)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(86400000L)))
                .build();
        redisTemplate.opsForValue().set("RefreshToken:" + username + ":" + newTokenId, newRefreshTokenDto, Duration.ofDays(1));

        // 기존 refresh token 삭제
        redisTemplate.delete(key);

        // 쿠키에 새로운 refreshToken 저장
        Cookie refreshCookie = cookieUtil.createCookie("RefreshToken", newRefreshToken);
        response.addCookie(refreshCookie);

        return new ReIssueTokenDto(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = request.getHeader("Authorization");
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7); // "Bearer " 제거
        }

        // 토큰에서 남은 만료 시간 계산
        long expiration = jwtProvider.getExpiration(accessToken);

        // Redis 블랙리스트에 저장 (TTL 설정)
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                    "BLACKLIST_" + accessToken,
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
        }

        String refreshToken = cookieUtil.getValue(request);
        String username = jwtProvider.getUsername(refreshToken);
        String tokenId = jwtProvider.getTokenId(refreshToken);

        String key = "RefreshToken:" + username + ":" + tokenId;

        redisTemplate.delete(key);

        // 쿠키에서 refresh token 제거
        Cookie refreshCookie = new Cookie("RefreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // 바로 만료
        response.addCookie(refreshCookie);
    }
}
