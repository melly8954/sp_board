package com.melly.sp_board.auth.security;

import com.melly.sp_board.member.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

// UserDetails 인터페이스는 인증 정보를 담은 사용자 객체를 정의
@Getter
public class PrincipalDetails implements UserDetails {
    private final Member member;

    // 일반 로그인용
    public PrincipalDetails(Member member) {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_" + member.getRole().name();
            }
        });
        return collection;
    }

    // Spring Security 인증 시 참조하는 실제 저장된 암호
    @Override
    public String getPassword() {
        return member.getPassword(); // DB에 저장된 BCrypt 암호
    }

    @Override
    public String getUsername() {
        return member.getUsername();
    }
}
