package com.melly.sp_board.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long memberId;
    private String username;
    private String role;
    private String tokenId;
    private String accessToken;
    private String refreshToken;
}
