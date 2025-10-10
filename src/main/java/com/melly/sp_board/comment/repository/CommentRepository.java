package com.melly.sp_board.comment.repository;

import com.melly.sp_board.comment.domain.Comment;
import com.melly.sp_board.comment.domain.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByCommentIdAndStatusNot(Long parentCommentId, CommentStatus commentStatus);

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.board.boardId = :boardId
            AND c.status = :status
            AND c.parent IS NULL
    """)
    Page<Comment> findParentComments(Pageable pageable, @Param("boardId") Long boardId, @Param("status") CommentStatus status);

    List<Comment> findByParentCommentIdInAndStatus(List<Long> parentIds, CommentStatus commentStatus);
}
