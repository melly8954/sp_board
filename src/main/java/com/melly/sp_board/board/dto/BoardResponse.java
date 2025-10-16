package com.melly.sp_board.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.melly.sp_board.filestorage.dto.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"owner", "admin"})
public class BoardResponse {
    private Long boardId;
    private String boardType;
    private String title;
    private String content;
    private String writerName;
    @JsonProperty("isOwner")
    private boolean isOwner;
    @JsonProperty("isAdmin")
    private boolean isAdmin;
    private Integer viewCount;
    private Integer likeCount;
    private List<FileDto> files;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
