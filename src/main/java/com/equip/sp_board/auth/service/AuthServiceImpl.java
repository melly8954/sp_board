package com.equip.sp_board.auth.service;

import com.equip.sp_board.auth.dto.LoginRequest;
import com.equip.sp_board.auth.dto.LoginResponse;
import com.equip.sp_board.auth.jwt.JwtProvider;
import com.equip.sp_board.auth.security.PrincipalDetails;
import com.equip.sp_board.common.exception.CustomException;
import com.equip.sp_board.common.exception.ErrorType;
import com.equip.sp_board.member.domain.Member;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

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

            return LoginResponse.builder()
                    .memberId(member.getMemberId())
                    .username(member.getUsername())
                    .tokenId(tokenId)
                    .accessToken(accessToken)
                    .role(member.getRole().name())
                    .build();
        } catch (BadCredentialsException e) {
            throw new CustomException(ErrorType.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        } catch (DisabledException e) {
            throw new CustomException(ErrorType.UNAUTHORIZED, "탈퇴 처리된 계정입니다.");
        }
    }
}
