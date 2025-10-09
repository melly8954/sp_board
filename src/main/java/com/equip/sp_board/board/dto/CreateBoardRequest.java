package com.equip.sp_board.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CreateBoardRequest {
    private Long boardTypeId;
    private String title;
    private String content;
}
