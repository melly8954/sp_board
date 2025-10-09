package com.melly.sp_board.member.controller;

import com.melly.sp_board.common.controller.ResponseController;
import com.melly.sp_board.common.dto.ResponseDto;
import com.melly.sp_board.common.trace.RequestTraceIdFilter;
import com.melly.sp_board.member.dto.CreateMemberRequest;
import com.melly.sp_board.member.dto.CreateMemberResponse;
import com.melly.sp_board.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController implements ResponseController {
    private final MemberService memberService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<CreateMemberResponse>> createMember(@RequestPart(value = "data") CreateMemberRequest dto,
                                                                          @RequestPart(value = "file") MultipartFile file){
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[회원가입 요청 API] TraceId={}", traceId);

        CreateMemberResponse result = memberService.createMember(dto, file);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "회원가입 요청 성공", result);
    }
}
