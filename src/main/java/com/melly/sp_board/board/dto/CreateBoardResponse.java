package com.melly.sp_board.board.dto;

import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.filestorage.dto.FileDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CreateBoardResponse {
    private Long boardId;
    private String boardType;
    private String title;
    private String content;
    private BoardStatus status;
    private List<FileDto> files;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
