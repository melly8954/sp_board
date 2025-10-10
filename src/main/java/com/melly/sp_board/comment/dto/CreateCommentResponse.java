package com.melly.sp_board.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreateCommentResponse {
    private Long commentId;
    private Long boardId;
    private Long writerId;
    private String content;
}
