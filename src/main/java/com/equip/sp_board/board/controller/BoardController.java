package com.equip.sp_board.board.controller;

import com.equip.sp_board.auth.security.PrincipalDetails;
import com.equip.sp_board.board.dto.CreateBoardRequest;
import com.equip.sp_board.board.dto.CreateBoardResponse;
import com.equip.sp_board.board.service.BoardService;
import com.equip.sp_board.common.controller.ResponseController;
import com.equip.sp_board.common.dto.ResponseDto;
import com.equip.sp_board.common.trace.RequestTraceIdFilter;
import com.equip.sp_board.member.domain.Member;
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
                                                                        @RequestPart(value = "files") List<MultipartFile> files,
                                                                        @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[게시글 등록 요청 API] TraceId={}", traceId);

        CreateBoardResponse result = boardService.createBoard(dto, files, principal.getMember().getMemberId());

        return makeResponseEntity(traceId, HttpStatus.OK, null, "게시글 등록 성공", result);
    }
}
