package com.melly.sp_board.board.domain;

import com.melly.sp_board.common.domain.BaseEntity;
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
}
