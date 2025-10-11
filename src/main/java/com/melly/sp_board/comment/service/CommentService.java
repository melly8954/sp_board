package com.melly.sp_board.comment.service;

import com.melly.sp_board.comment.dto.*;
import com.melly.sp_board.common.dto.PageResponseDto;

public interface CommentService {
    CreateCommentResponse createComment(CreateCommentRequest dto, Long currentUserId);

    PageResponseDto<CommentListResponse> getCommentList(CommentFilter filter, Long currentUserId);

    UpdateCommentResponse updateComment(Long commentId, UpdateCommentRequest dto, Long currentUserId);

    void softDeleteComment(Long commentId, Long currentUserId);
}
