package com.melly.sp_board.member.service;

import com.melly.sp_board.member.dto.CreateMemberRequest;
import com.melly.sp_board.member.dto.CreateMemberResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {
    CreateMemberResponse createMember(CreateMemberRequest dto, MultipartFile file);
}
