package com.melly.sp_board.auth.service;

import com.melly.sp_board.auth.dto.LoginRequest;
import com.melly.sp_board.auth.dto.LoginResponse;
import com.melly.sp_board.auth.dto.RefreshTokenDto;
import com.melly.sp_board.auth.jwt.JwtProvider;
import com.melly.sp_board.auth.security.PrincipalDetails;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.common.util.CookieUtil;
import com.melly.sp_board.member.domain.Member;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;
    private final RedisTemplate<String, Object> redisTemplate;

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
}
