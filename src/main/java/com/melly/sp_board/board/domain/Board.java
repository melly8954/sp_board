package com.melly.sp_board.board.domain;

import com.melly.sp_board.board.dto.UpdateBoardRequest;
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
@Table(name="board_tbl")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="board_id")
    private Long boardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="board_type_id")
    private BoardType boardType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="writer_id")
    private Member writer;

    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;

    @Enumerated(EnumType.STRING)
    private BoardStatus status;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    public void updateBoard(UpdateBoardRequest dto, Long currentUserId) {
        if(!this.getWriter().getMemberId().equals(currentUserId)) {
            throw new CustomException(ErrorType.FORBIDDEN, "본인 게시글이 아니면 수정할 수 없습니다.");
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            this.title = dto.getTitle();
        }
        if (dto.getContent() != null && !dto.getContent().isBlank()) {
            this.content = dto.getContent();
        }
    }

    public void softDeleteBoard(Member currentUser) {
        if((!this.getWriter().getMemberId().equals(currentUser.getMemberId())) && !currentUser.isAdmin() ) {
            throw new CustomException(ErrorType.FORBIDDEN, "본인 게시글 또는 관리자가 아니면 삭제할 수 없습니다.");
        }
        this.status = BoardStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 1;
        } else {
            this.viewCount++;
        }
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount--;
    }
}
