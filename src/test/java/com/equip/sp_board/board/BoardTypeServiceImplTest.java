package com.equip.sp_board.board;

import com.melly.sp_board.board.domain.BoardType;
import com.melly.sp_board.board.dto.BoardTypeResponse;
import com.melly.sp_board.board.repository.BoardTypeRepository;
import com.melly.sp_board.board.service.BoardTypeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardTypeServiceImpl 단위 테스트")
class BoardTypeServiceImplTest {

    @Mock
    BoardTypeRepository boardTypeRepository;

    @InjectMocks
    BoardTypeServiceImpl boardTypeService;

    @Test
    @DisplayName("getBoardTypes() - 정상 조회")
    void testGetBoardTypes() {
        // given
        BoardType boardType1 = new BoardType(1L, "공지", "공지 게시판 전용 타입");
        BoardType boardType2 = new BoardType(2L, "자유", "자유 게시판 전용 타입");

        when(boardTypeRepository.findAll()).thenReturn(List.of(boardType1, boardType2));

        // when
        List<BoardTypeResponse> result = boardTypeService.getBoardTypes();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBoardTypeName()).isEqualTo("공지");
        assertThat(result.get(1).getBoardTypeName()).isEqualTo("자유");

        verify(boardTypeRepository, times(1)).findAll();
    }
}
