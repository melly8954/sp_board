package com.equip.sp_board.member.service;

import com.equip.sp_board.member.dto.CreateMemberRequest;
import com.equip.sp_board.member.dto.CreateMemberResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {
    CreateMemberResponse createMember(CreateMemberRequest dto, MultipartFile file);
}
