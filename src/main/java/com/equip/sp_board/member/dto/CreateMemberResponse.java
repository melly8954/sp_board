package com.equip.sp_board.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CreateMemberResponse {
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "아이디", example = "testUser123")
    private String username;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "회원가입 일시", example = "2025-10-08 22:30:00")
    private LocalDateTime createdAt;
}
