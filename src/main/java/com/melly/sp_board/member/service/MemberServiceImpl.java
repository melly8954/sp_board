package com.melly.sp_board.member.service;

import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.filestorage.domain.FileMeta;
import com.melly.sp_board.filestorage.repository.FileRepository;
import com.melly.sp_board.filestorage.service.iface.FileService;
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.domain.MemberRole;
import com.melly.sp_board.member.domain.MemberStatus;
import com.melly.sp_board.member.dto.CreateMemberRequest;
import com.melly.sp_board.member.dto.CreateMemberResponse;
import com.melly.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileRepository fileRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public CreateMemberResponse createMember(CreateMemberRequest dto, MultipartFile file) {
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

        String profileImageUrl = "";
        if (file != null && !file.isEmpty()) {
            profileImageUrl = fileService.saveFile(file, "member");

            FileMeta fileMeta = FileMeta.builder()
                    .relatedType("member")               // 어떤 엔티티와 연관된 파일인지
                    .relatedId(member.getMemberId())      // 방금 생성된 멤버 ID
                    .originalName(file.getOriginalFilename())
                    .uniqueName(profileImageUrl.substring(profileImageUrl.lastIndexOf("/") + 1))
                    .filePath(profileImageUrl)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileOrder(1)
                    .build();
            fileRepository.save(fileMeta);
        }

        return CreateMemberResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .profileImageUrl(profileImageUrl)
                .createdAt(member.getCreatedAt())
                .build();
    }
}
