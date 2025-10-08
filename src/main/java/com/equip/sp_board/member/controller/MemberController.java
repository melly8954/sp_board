package com.equip.sp_board.member.controller;

import com.equip.sp_board.common.controller.ResponseController;
import com.equip.sp_board.common.dto.ResponseDto;
import com.equip.sp_board.common.trace.RequestTraceIdFilter;
import com.equip.sp_board.member.dto.CreateMemberRequest;
import com.equip.sp_board.member.dto.CreateMemberResponse;
import com.equip.sp_board.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "회원", description = "")
public class MemberController implements ResponseController {
    private final MemberService memberService;

    @PostMapping("")
    @Operation(summary = "회원가입", description = "신규 회원을 등록합니다.")
    public ResponseEntity<ResponseDto<CreateMemberResponse>> createMember(@RequestBody CreateMemberRequest dto){
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[회원가입 요청 API] TraceId={}", traceId);

        CreateMemberResponse result = memberService.createMember(dto);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "회원가입 요청 성공", result);
    }
}
