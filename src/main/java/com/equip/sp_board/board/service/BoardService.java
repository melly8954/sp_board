package com.equip.sp_board.board.service;

import com.equip.sp_board.board.dto.CreateBoardRequest;
import com.equip.sp_board.board.dto.CreateBoardResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {
    CreateBoardResponse createBoard(CreateBoardRequest dto, List<MultipartFile> files, Long memberId);
}
