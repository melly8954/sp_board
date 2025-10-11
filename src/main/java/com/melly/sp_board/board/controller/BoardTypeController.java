package com.melly.sp_board.board.controller;

import com.melly.sp_board.board.dto.BoardTypeResponse;
import com.melly.sp_board.board.service.BoardTypeService;
import com.melly.sp_board.common.controller.ResponseController;
import com.melly.sp_board.common.dto.ResponseDto;
import com.melly.sp_board.common.trace.RequestTraceIdFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards/types")
public class BoardTypeController implements ResponseController {
    private final BoardTypeService boardTypeService;

    @GetMapping("")
    public ResponseEntity<ResponseDto<List<BoardTypeResponse>>> getBoardTypes() {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[게시판 타입 조회 요청 API] TraceId={}", traceId);

        List<BoardTypeResponse> result = boardTypeService.getBoardTypes();
        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시판 타입 조회 성공", result);
    }
}
