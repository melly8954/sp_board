package com.melly.sp_board.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CommentListResponse {
    private Long commentId;
    private Long writerId;
    private String writerName;
    private Boolean isOwner;
    private String content;
    private Integer likeCount;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @Builder.Default
    private List<CommentListResponse> children = new ArrayList<>();
}
