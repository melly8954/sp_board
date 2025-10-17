package com.melly.sp_board.like.service;

public interface LikeService {
    String toggleBoardLike(Long boardId, Long currentUserId);
    String toggleCommentLike(Long commentId, Long currentUserId);
}
