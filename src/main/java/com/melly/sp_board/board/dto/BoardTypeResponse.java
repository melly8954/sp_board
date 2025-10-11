package com.melly.sp_board.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BoardTypeResponse {
    private Long boardTypeId;
    private String boardTypeName;
}
