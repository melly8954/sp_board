package com.melly.sp_board.comment.controller;

import com.melly.sp_board.auth.security.PrincipalDetails;
import com.melly.sp_board.comment.dto.CreateCommentRequest;
import com.melly.sp_board.comment.dto.CreateCommentResponse;
import com.melly.sp_board.comment.service.CommentService;
import com.melly.sp_board.common.controller.ResponseController;
import com.melly.sp_board.common.dto.ResponseDto;
import com.melly.sp_board.common.trace.RequestTraceIdFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
