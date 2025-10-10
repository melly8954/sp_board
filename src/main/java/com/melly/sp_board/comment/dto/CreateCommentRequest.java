package com.melly.sp_board.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreateCommentRequest {
    private Long boardId;
    private Long parentCommentId;
    private String content;
}
