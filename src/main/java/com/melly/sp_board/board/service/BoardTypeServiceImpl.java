package com.melly.sp_board.board.service;

import com.melly.sp_board.board.dto.BoardTypeResponse;
import com.melly.sp_board.board.repository.BoardTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardTypeServiceImpl implements BoardTypeService {
    private final BoardTypeRepository boardTypeRepository;

    @Override
    @Transactional
    public List<BoardTypeResponse> getBoardTypes() {
        return boardTypeRepository.findAll()
                .stream()
                .map(boardType -> BoardTypeResponse.builder()
                        .boardTypeId(boardType.getBoardTypeId())
                        .boardTypeCode(boardType.getCode())
                        .boardTypeName(boardType.getName())
                        .build())
                .toList();
    }
}
