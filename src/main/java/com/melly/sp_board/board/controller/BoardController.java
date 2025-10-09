package com.melly.sp_board.board.controller;

import com.melly.sp_board.auth.security.PrincipalDetails;
import com.melly.sp_board.board.dto.BoardFilter;
import com.melly.sp_board.board.dto.BoardResponse;
import com.melly.sp_board.board.dto.CreateBoardRequest;
import com.melly.sp_board.board.dto.CreateBoardResponse;
import com.melly.sp_board.board.service.BoardService;
import com.melly.sp_board.common.controller.ResponseController;
import com.melly.sp_board.common.dto.PageResponseDto;
import com.melly.sp_board.common.dto.ResponseDto;
import com.melly.sp_board.common.trace.RequestTraceIdFilter;
import com.melly.sp_board.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class BoardController implements ResponseController {
    private final BoardService boardService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<CreateBoardResponse>> createBoard(@RequestPart(value = "data") CreateBoardRequest dto,
                                                                        @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                                        @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[게시글 등록 요청 API] TraceId={}", traceId);

        CreateBoardResponse result = boardService.createBoard(dto, files, principal.getMember().getMemberId());

        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 등록 성공", result);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<PageResponseDto<BoardResponse>>> searchBoard(@ModelAttribute BoardFilter filter) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[게시글 목록 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<BoardResponse> result = boardService.searchBoard(filter);

        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 목록 조회 성공", result);
    }
}
