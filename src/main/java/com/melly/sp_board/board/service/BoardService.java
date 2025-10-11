package com.melly.sp_board.board.service;

import com.melly.sp_board.board.dto.*;
import com.melly.sp_board.common.dto.PageResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {
    CreateBoardResponse createBoard(CreateBoardRequest dto, List<MultipartFile> files, Long memberId);

    PageResponseDto<BoardListResponse> searchBoard(BoardFilter filter);

    BoardResponse getBoard(Long boardId, Long currentUserId);

    UpdateBoardResponse updateBoard(Long boardId, UpdateBoardRequest dto, List<MultipartFile> newFiles, Long currentUserId);

    void softDeleteBoard(Long boardId, Long currentUserId);
}
