package com.melly.sp_board.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class UpdateBoardRequest {
    private String title;
    private String content;
    private List<Long> removeFileIds;
}
