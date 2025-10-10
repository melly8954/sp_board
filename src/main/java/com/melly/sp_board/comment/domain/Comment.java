package com.melly.sp_board.comment.domain;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.common.domain.BaseEntity;
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
}
