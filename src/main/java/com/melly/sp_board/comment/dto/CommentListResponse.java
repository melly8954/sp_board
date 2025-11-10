package com.melly.sp_board.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"owner", "liked"})
public class CommentListResponse {
    private Long commentId;
    private Long writerId;
    private String writerName;
    @JsonProperty("isOwner")
    private Boolean isOwner;
    private String content;
    @JsonProperty("isLiked")
    private boolean isLiked;
    private Integer likeCount;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @Builder.Default
    private List<CommentListResponse> children = new ArrayList<>();
}
