package com.equip.sp_board.board;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardType;
import com.melly.sp_board.board.dto.CreateBoardRequest;
import com.melly.sp_board.board.dto.CreateBoardResponse;
import com.melly.sp_board.board.repository.BoardRepository;
import com.melly.sp_board.board.repository.BoardTypeRepository;
import com.melly.sp_board.board.service.BoardServiceImpl;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.filestorage.repository.FileRepository;
import com.melly.sp_board.filestorage.service.iface.FileService;
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardServiceImpl 단위 테스트")
public class BoardServiceImplTest {
    @Mock MemberRepository memberRepository;
    @Mock BoardRepository boardRepository;
    @Mock BoardTypeRepository boardTypeRepository;
    @Mock FileService fileService;
    @Mock FileRepository fileRepository;

    @InjectMocks
    BoardServiceImpl boardService;

    @Nested
    @DisplayName("createBoard() 메서드 테스트")
    class CreateBoard {
        @Test
        @DisplayName("성공 - 게시글 등록 (No_File)")
        void createBoard_Success_NoFiles() {
            // given
            Long memberId = 1L;
            CreateBoardRequest dto = CreateBoardRequest.builder()
                    .boardTypeId(1L)
                    .build();

            Member member = new Member();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용 타입");

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(boardTypeRepository.findById(dto.getBoardTypeId())).thenReturn(Optional.of(boardType));

            // when
            CreateBoardResponse response = boardService.createBoard(dto, null, memberId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getBoardType()).isEqualTo("공지");
            assertThat(response.getFiles()).isEmpty();

            verify(boardRepository, times(1)).save(any(Board.class));
            verify(fileService, never()).saveFiles(any(), anyString());
            verify(fileRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("성공 - 게시글 등록 (With_File)")
        void createBoard_Success_WithFiles() {
            // given
            Long memberId = 1L;
            CreateBoardRequest dto = CreateBoardRequest.builder()
                    .boardTypeId(1L)
                    .build();

            Member member = new Member();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용 타입");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.png",
                    "image/png",
                    "test image".getBytes()
            );

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(boardTypeRepository.findById(dto.getBoardTypeId())).thenReturn(Optional.of(boardType));

            when(fileService.saveFiles(any(), anyString())).thenReturn(List.of("/files/board_공지/test.png"));
            when(fileRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            CreateBoardResponse response = boardService.createBoard(dto, List.of(file), memberId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getFiles()).hasSize(1);
            assertThat(response.getFiles().get(0).getFilePath()).isEqualTo("/files/board_공지/test.png");

            verify(boardRepository, times(1)).save(any(Board.class));
            verify(fileService, times(1)).saveFiles(any(), anyString());
            verify(fileRepository, times(1)).saveAll(any());
        }

        @Test
        @DisplayName("예외 - 회원 없음")
        void createBoard_MemberNotFound() {
            // given
            Long memberId = 1L;
            CreateBoardRequest dto = CreateBoardRequest.builder()
                    .boardTypeId(1L)
                    .title("제목")
                    .content("내용")
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.createBoard(dto, null, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(boardRepository, never()).save(any());
        }

        @Test
        @DisplayName("예외 - 게시판 타입 없음")
        void createBoard_BoardTypeNotFound() {
            // given
            Long memberId = 1L;
            CreateBoardRequest dto = CreateBoardRequest.builder()
                    .boardTypeId(1L)
                    .title("제목")
                    .content("내용")
                    .build();

            Member member = new Member();
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(boardTypeRepository.findById(dto.getBoardTypeId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.createBoard(dto, null, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(boardRepository, never()).save(any());
        }
    }
}
