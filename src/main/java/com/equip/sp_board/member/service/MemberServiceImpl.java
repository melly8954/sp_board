package com.equip.sp_board.member.service;

import com.equip.sp_board.common.exception.CustomException;
import com.equip.sp_board.common.exception.ErrorType;
import com.equip.sp_board.member.domain.Member;
import com.equip.sp_board.member.domain.MemberRole;
import com.equip.sp_board.member.domain.MemberStatus;
import com.equip.sp_board.member.dto.CreateMemberRequest;
import com.equip.sp_board.member.dto.CreateMemberResponse;
import com.equip.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CreateMemberResponse createMember(CreateMemberRequest dto) {
        if(memberRepository.existsByUsername(dto.getUsername())){
            throw new CustomException(ErrorType.BAD_REQUEST, "이미 사용 중인 username 입니다.");
        }
        if(!dto.getPassword().equals(dto.getConfirmPassword())){
            throw new CustomException(ErrorType.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        Member member = Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .role(MemberRole.USER)
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(member);

        return CreateMemberResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
