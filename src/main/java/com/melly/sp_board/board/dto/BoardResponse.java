package com.melly.sp_board.board.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.melly.sp_board.filestorage.dto.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BoardResponse {
    private Long boardId;
    private String boardType;
    private String title;
    private String content;
    private String writerName;
    private boolean isOwner;
    private Integer viewCount;
    private Integer likeCount;
    private List<FileDto> files;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
