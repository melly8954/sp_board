package com.melly.sp_board.board.dto;

import com.melly.sp_board.common.dto.SearchParamDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BoardFilter extends SearchParamDto {
    private Long boardTypeId;
    private String searchType;
    private String searchKeyword;
}
