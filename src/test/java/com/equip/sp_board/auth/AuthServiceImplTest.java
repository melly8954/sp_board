package com.equip.sp_board.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melly.sp_board.auth.dto.LoginRequest;
import com.melly.sp_board.auth.dto.LoginResponse;
import com.melly.sp_board.auth.dto.ReIssueTokenDto;
import com.melly.sp_board.auth.dto.RefreshTokenDto;
import com.melly.sp_board.auth.jwt.JwtProvider;
import com.melly.sp_board.auth.security.PrincipalDetails;
import com.melly.sp_board.auth.service.AuthServiceImpl;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.common.util.CookieUtil;
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.domain.MemberRole;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl 단위 테스트")
public class AuthServiceImplTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtProvider jwtProvider;
    @Mock CookieUtil cookieUtil;
    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOperations;
    @Mock ObjectMapper objectMapper;
    @Mock Authentication authentication;
    @Mock PrincipalDetails principalDetails;
    @Mock Member member;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;

    @InjectMocks AuthServiceImpl authServiceImpl;

    @Nested
    @DisplayName("login() 메서드 테스트")
    class login {
        @Test
        @DisplayName("성공 - 로그인 성공")
        void loginSuccess() {
            // given
            LoginRequest dto = new LoginRequest("testuser", "password");

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(principalDetails);
            when(principalDetails.getMember()).thenReturn(member);
            when(member.getMemberId()).thenReturn(1L);
            when(member.getUsername()).thenReturn("testuser");
            when(member.getRole()).thenReturn(MemberRole.USER);
            when(jwtProvider.createJwt(anyString(), eq("AccessToken"), anyString(), anyString(), anyLong()))
                    .thenReturn("AccessToken");
            when(jwtProvider.createJwt(anyString(), eq("RefreshToken"), anyString(), anyString(), anyLong()))
                    .thenReturn("RefreshToken");
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doNothing().when(valueOperations).set(anyString(), any(), any(Duration.class));

            // when
            LoginResponse result = authServiceImpl.login(dto, response);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMemberId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getRole()).isEqualTo("USER");
            assertThat(result.getAccessToken()).isEqualTo("AccessToken");
            assertThat(result.getRefreshToken()).isEqualTo("RefreshToken");

            // 쿠키가 response에 추가되었는지 확인
            verify(response, times(1)).addCookie(any());
            verify(valueOperations).set(anyString(), any(), any(Duration.class));
        }

        @Test
        @DisplayName("예외 - 비밀번호 불일치")
        void loginBadCredentials() {
            // given
            LoginRequest dto = new LoginRequest("testuser", "wrongpassword");

            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("BAD_CREDENTIALS"));

            // when & then
            assertThatThrownBy(() -> authServiceImpl.login(dto, response))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @Test
        @DisplayName("예외 - 회원 탈퇴")
        void loginUserDeleted() {
            // given
            LoginRequest dto = new LoginRequest("testuser", "password");

            DisabledException disabledException = new DisabledException("USER_DELETED");
            when(authenticationManager.authenticate(any())).thenThrow(disabledException);

            assertThatThrownBy(() -> authServiceImpl.login(dto, response))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("reissueToken() 메서드 테스트")
    class reissueToken {
        @Test
        @DisplayName("성공 - 토큰 재발급")
        void testReissueTokenSuccess() {
            when(cookieUtil.getValue(request)).thenReturn("mockRefreshToken");
            when(jwtProvider.getTokenType("mockRefreshToken")).thenReturn("RefreshToken");
            when(jwtProvider.isExpired("mockRefreshToken")).thenReturn(false);
            when(jwtProvider.getUsername("mockRefreshToken")).thenReturn("testuser");
            when(jwtProvider.getTokenId("mockRefreshToken")).thenReturn("tokenId");

            RefreshTokenDto existingRefreshTokenDto = RefreshTokenDto.builder()
                    .username("testuser")
                    .role("USER")
                    .tokenId("tokenId")
                    .build();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("RefreshToken:testuser:tokenId")).thenReturn(existingRefreshTokenDto);
            when(objectMapper.convertValue(existingRefreshTokenDto, RefreshTokenDto.class))
                    .thenReturn(existingRefreshTokenDto);

            when(jwtProvider.createJwt(anyString(), eq("AccessToken"), anyString(), anyString(), anyLong()))
                    .thenReturn("newAccessToken");
            when(jwtProvider.createJwt(anyString(), eq("RefreshToken"), anyString(), anyString(), anyLong()))
                    .thenReturn("newRefreshToken");

            doNothing().when(valueOperations).set(anyString(), any(), any(Duration.class));
            when(redisTemplate.delete(anyString())).thenReturn(true);
            when(cookieUtil.createCookie(eq("RefreshToken"), eq("newRefreshToken")))
                    .thenReturn(new Cookie("RefreshToken", "newRefreshToken"));

            // when
            ReIssueTokenDto result = authServiceImpl.reissueToken(request, response);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getNewAccessToken()).isEqualTo("newAccessToken");
            assertThat(result.getNewRefreshToken()).isEqualTo("newRefreshToken");

            verify(valueOperations).set(anyString(), any(), any(Duration.class));
            verify(redisTemplate).delete("RefreshToken:testuser:tokenId");
            verify(response).addCookie(any());
        }

        @Test
        @DisplayName("예외 - 쿠키에 RefreshToken 없음")
        void testReissueToken_NoToken() {
            when(cookieUtil.getValue(request)).thenReturn(null);

            assertThatThrownBy(() -> authServiceImpl.reissueToken(request, response))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 잘못된 RefreshToken 타입")
        void testReissueToken_InvalidType() {
            when(cookieUtil.getValue(request)).thenReturn("mockToken");
            when(jwtProvider.getTokenType("mockToken")).thenReturn("AccessToken");

            assertThatThrownBy(() -> authServiceImpl.reissueToken(request, response))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @Test
        @DisplayName("예외 - RefreshToken 만료")
        void testReissueToken_Expired() {
            when(cookieUtil.getValue(request)).thenReturn("mockToken");
            when(jwtProvider.getTokenType("mockToken")).thenReturn("RefreshToken");
            when(jwtProvider.isExpired("mockToken")).thenReturn(true);

            assertThatThrownBy(() -> authServiceImpl.reissueToken(request, response))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @Test
        @DisplayName("예외 - Redis에 토큰 없음")
        void testReissueToken_RedisMissing() {
            when(cookieUtil.getValue(request)).thenReturn("mockToken");
            when(jwtProvider.getTokenType("mockToken")).thenReturn("RefreshToken");
            when(jwtProvider.isExpired("mockToken")).thenReturn(false);
            when(jwtProvider.getUsername("mockToken")).thenReturn("testuser");
            when(jwtProvider.getTokenId("mockToken")).thenReturn("tokenId");

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("RefreshToken:testuser:tokenId")).thenReturn(null);

            assertThatThrownBy(() -> authServiceImpl.reissueToken(request, response))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("logout() 메서드 테스트")
    class logout {
        @Test
        @DisplayName("성공 - 로그아웃 성공")
        void logout_success() {
            String accessToken = "AccessToken";
            String refreshToken = "RefreshToken";

            // Header mock
            when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);

            // Redis TTL mock
            when(jwtProvider.getExpiration(accessToken)).thenReturn(1000L); // 1초

            // Refresh token mock
            when(cookieUtil.getValue(request)).thenReturn(refreshToken);
            when(jwtProvider.getUsername(refreshToken)).thenReturn("testuser");
            when(jwtProvider.getTokenId(refreshToken)).thenReturn("tokenId");

            // Redis mock
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
            when(redisTemplate.delete(anyString())).thenReturn(true);

            // Response cookie mock
            doNothing().when(response).addCookie(any(Cookie.class));

            authServiceImpl.logout(request, response);

            // 검증
            verify(valueOperations).set(
                    eq("BLACKLIST_" + accessToken),
                    eq("logout"),
                    eq(1000L),
                    eq(TimeUnit.MILLISECONDS)
            );
            verify(redisTemplate).delete("RefreshToken:testuser:tokenId");
            verify(response).addCookie(any(Cookie.class));
        }
    }
}
