package com.melly.sp_board.auth.controller;

import com.melly.sp_board.auth.dto.LoginRequest;
import com.melly.sp_board.auth.dto.LoginResponse;
import com.melly.sp_board.auth.dto.ReIssueTokenDto;
import com.melly.sp_board.auth.service.AuthService;
import com.melly.sp_board.common.controller.ResponseController;
import com.melly.sp_board.common.dto.ResponseDto;
import com.melly.sp_board.common.trace.RequestTraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements ResponseController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<LoginResponse>> jwtLogin(@RequestBody LoginRequest dto, HttpServletResponse response) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[로그인 요청 API] TraceId={}", traceId);

        LoginResponse result = authService.login(dto,response);
        return makeResponseEntity(traceId, HttpStatus.OK, null, "로그인 성공", result);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ResponseDto<ReIssueTokenDto>> reissueToken(HttpServletRequest request, HttpServletResponse response){
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[토큰 재발급 요청 API] TraceId={}", traceId);

        ReIssueTokenDto result = authService.reissueToken(request, response);
        return makeResponseEntity(traceId, HttpStatus.OK, "null","토큰 재발급 성공", result);
    }


    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(HttpServletRequest request, HttpServletResponse response){
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[로그아웃 요청 API] TraceId={}", traceId);

        authService.logout(request,response);
        return makeResponseEntity(traceId, HttpStatus.OK, "null","로그아웃 성공", null);
    }
}
