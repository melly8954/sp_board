package com.melly.sp_board.board.service;

import com.melly.sp_board.board.dto.BoardFilter;
import com.melly.sp_board.board.dto.BoardListResponse;
import com.melly.sp_board.board.dto.CreateBoardRequest;
import com.melly.sp_board.board.dto.CreateBoardResponse;
import com.melly.sp_board.common.dto.PageResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {
    CreateBoardResponse createBoard(CreateBoardRequest dto, List<MultipartFile> files, Long memberId);

    PageResponseDto<BoardListResponse> searchBoard(BoardFilter filter);
}
