package com.melly.sp_board.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberDto {
    private Long memberId;
    private String username;
    private String name;
    private String profileImage;
}
