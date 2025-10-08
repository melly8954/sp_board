package com.equip.sp_board.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long memberId;
    private String username;
    private String tokenId;
    private String accessToken;
    private String role;
}
