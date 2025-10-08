package com.equip.sp_board.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreateMemberRequest {
    @Schema(description = "아이디", example = "testUser123")
    private String username;

    @Schema(description = "비밀번호", example = "1q2w3e4r")
    private String password;

    @Schema(description = "비밀번호 확인", example = "1q2w3e4r!")
    private String confirmPassword;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
}
