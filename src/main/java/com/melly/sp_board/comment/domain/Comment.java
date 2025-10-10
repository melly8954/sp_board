package com.melly.sp_board.comment.domain;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.comment.dto.UpdateCommentRequest;
import com.melly.sp_board.common.domain.BaseEntity;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="comment_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="board_id")
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="writer_id")
    private Member writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Comment parent;

    private String content;

    @Column(name="like_count")
    private Integer likeCount;

    @Enumerated(EnumType.STRING)
    private CommentStatus status;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    public void updateComment(Member currentUser, UpdateCommentRequest dto) {
        if((!this.getWriter().getMemberId().equals(currentUser.getMemberId())) && !currentUser.isAdmin() ) {
            throw new CustomException(ErrorType.FORBIDDEN, "본인 게시글 또는 관리자가 아니면 수정할 수 없습니다.");
        }
        this.content = dto.getContent();
    }
}
