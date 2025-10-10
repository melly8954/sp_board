package com.melly.sp_board.comment.dto;

import com.melly.sp_board.common.dto.SearchParamDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CommentFilter extends SearchParamDto {
    private Long boardId;
}
