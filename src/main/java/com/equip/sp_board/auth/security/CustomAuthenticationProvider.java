package com.equip.sp_board.auth.security;

import com.equip.sp_board.member.domain.Member;
import com.equip.sp_board.member.domain.MemberStatus;
import com.equip.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        // 사용자 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("BAD_CREDENTIALS"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BadCredentialsException("BAD_CREDENTIALS");
        }

        if (member.getStatus() == MemberStatus.DELETED) {
            throw new DisabledException("USER_DELETED");
        }

        // 인증 토큰 생성 (권한 정보 등 추가 가능)
        PrincipalDetails principalDetails = new PrincipalDetails(member);

        String roleName = member.getRole().name(); // 예: ADMIN
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + roleName)
        );

        return new UsernamePasswordAuthenticationToken(principalDetails, null, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
