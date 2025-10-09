package com.melly.sp_board.auth.security;

import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 회원은 존재하지 않습니다."));

        return new PrincipalDetails(member);
    }
}
