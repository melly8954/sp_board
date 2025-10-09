package com.melly.sp_board.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginRequest {
    private String username;
    private String password;
}
