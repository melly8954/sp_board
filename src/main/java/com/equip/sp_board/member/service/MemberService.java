package com.equip.sp_board.member.service;

import com.equip.sp_board.member.dto.CreateMemberRequest;
import com.equip.sp_board.member.dto.CreateMemberResponse;

public interface MemberService {
    CreateMemberResponse createMember(CreateMemberRequest dto);
}
