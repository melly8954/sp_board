package com.melly.sp_board.comment.controller;

import com.melly.sp_board.auth.security.PrincipalDetails;
import com.melly.sp_board.comment.dto.*;
import com.melly.sp_board.comment.service.CommentService;
import com.melly.sp_board.common.controller.ResponseController;
import com.melly.sp_board.common.dto.PageResponseDto;
import com.melly.sp_board.common.dto.ResponseDto;
import com.melly.sp_board.common.trace.RequestTraceIdFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController implements ResponseController {
    private final CommentService commentService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<CreateCommentResponse>> createComment(@RequestBody CreateCommentRequest dto,
                                                                            @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[댓글 등록 요청 API] TraceId={}", traceId);

        CreateCommentResponse result = commentService.createComment(dto, principal.getMember().getMemberId());

        return makeResponseEntity(traceId, HttpStatus.OK, null, "댓글 등록 성공", result);
    }

    @GetMapping("")
    public ResponseEntity<ResponseDto<PageResponseDto<CommentListResponse>>> getCommentList(@ModelAttribute CommentFilter filter,
                                                                                            @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[댓글 목록 조회 요청 API] TraceId={}", traceId);

        PageResponseDto<CommentListResponse> result = commentService.getCommentList(filter, principal.getMember().getMemberId());

        return makeResponseEntity(traceId, HttpStatus.OK, null, "댓글 목록 조회 성공", result);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ResponseDto<UpdateCommentResponse>> updateComment(@PathVariable Long commentId,
                                                                            @RequestBody UpdateCommentRequest dto,
                                                                            @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[댓글 수정 요청 API] TraceId={}", traceId);

        UpdateCommentResponse result = commentService.updateComment(commentId, dto, principal.getMember().getMemberId());

        return makeResponseEntity(traceId, HttpStatus.OK, null, "댓글 수정 성공", result);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseDto<Void>> softDeleteComment(@PathVariable Long commentId,
                                                           @AuthenticationPrincipal PrincipalDetails principal) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.info("[댓글 삭제 요청 API] TraceId={}", traceId);

        commentService.softDeleteComment(commentId, principal.getMember().getMemberId());

        return makeResponseEntity(traceId, HttpStatus.OK, null, "댓글 삭제 성공", null);
    }
}
