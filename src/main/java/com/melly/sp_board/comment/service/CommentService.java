package com.melly.sp_board.comment.service;

import com.melly.sp_board.comment.dto.CreateCommentRequest;
import com.melly.sp_board.comment.dto.CreateCommentResponse;

public interface CommentService {
    CreateCommentResponse createComment(CreateCommentRequest dto, Long currentUserId);
}
