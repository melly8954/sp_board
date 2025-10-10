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
public class UpdateBoardResponse {
    private Long boardId;
    private String title;
    private String content;
    private List<FileDto> files;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
