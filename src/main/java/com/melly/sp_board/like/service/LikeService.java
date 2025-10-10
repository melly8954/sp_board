package com.melly.sp_board.like.service;

public interface LikeService {
    String toggleBoardLike(Long boardId, Long memberId);
    void toggleCommentLike(Long commentId, Long memberId);
}
