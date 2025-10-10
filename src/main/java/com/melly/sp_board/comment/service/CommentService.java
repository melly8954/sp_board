package com.melly.sp_board.comment.service;

import com.melly.sp_board.comment.dto.CommentFilter;
import com.melly.sp_board.comment.dto.CommentListResponse;
import com.melly.sp_board.comment.dto.CreateCommentRequest;
import com.melly.sp_board.comment.dto.CreateCommentResponse;
import com.melly.sp_board.common.dto.PageResponseDto;

public interface CommentService {
    CreateCommentResponse createComment(CreateCommentRequest dto, Long currentUserId);

    PageResponseDto<CommentListResponse> getCommentList(CommentFilter filter, Long currentUserId);
}
