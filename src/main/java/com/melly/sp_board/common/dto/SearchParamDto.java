package com.melly.sp_board.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SearchParamDto {
    private int page;
    private int size;
}
