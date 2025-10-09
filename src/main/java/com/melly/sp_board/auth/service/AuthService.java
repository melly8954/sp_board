package com.melly.sp_board.auth.service;

import com.melly.sp_board.auth.dto.LoginRequest;
import com.melly.sp_board.auth.dto.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponse login(LoginRequest dto, HttpServletResponse response);
}
