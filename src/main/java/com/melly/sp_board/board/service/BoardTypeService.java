package com.melly.sp_board.board.service;

import com.melly.sp_board.board.dto.BoardTypeResponse;

import java.util.List;

public interface BoardTypeService {
    List<BoardTypeResponse> getBoardTypes();
}
