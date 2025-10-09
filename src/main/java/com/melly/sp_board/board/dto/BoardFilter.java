package com.melly.sp_board.board.dto;

import com.melly.sp_board.board.domain.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BoardFilter {
    private BoardType boardType;
}
