package com.equip.sp_board.board;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.board.domain.BoardType;
import com.melly.sp_board.board.dto.*;
import com.melly.sp_board.board.repository.BoardRepository;
import com.melly.sp_board.board.repository.BoardTypeRepository;
import com.melly.sp_board.board.service.BoardServiceImpl;
import com.melly.sp_board.common.dto.PageResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOperations;
    @Mock SetOperations<String, Object> setOperations;

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

    @Nested
    @DisplayName("searchBoard() 단위 테스트")
    class searchBoard {
        @Test
        @DisplayName("성공 - 게시글 목록 조회")
        void searchBoard_Success() {
            // given
            BoardFilter filter = BoardFilter.builder()
                    .boardTypeId(1L)
                    .searchKeyword("테스트")
                    .searchType("title")
                    .page(1)
                    .size(10)
                    .build();

            Pageable pageable = filter.getPageable();

            Member writer = Member.builder()
                    .name("작성자")
                    .build();

            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용");

            Board board1 = Board.builder()
                    .boardId(1L)
                    .boardType(boardType)
                    .writer(writer)
                    .title("테스트 게시글1")
                    .viewCount(1)
                    .likeCount(1)
                    .build();

            Board board2 = Board.builder()
                    .boardId(2L)
                    .boardType(boardType)
                    .writer(writer)
                    .title("테스트 게시글2")
                    .viewCount(2)
                    .likeCount(2)
                    .build();

            List<Board> boards = List.of(board1, board2);
            Page<Board> page = new PageImpl<>(boards, pageable, boards.size());

            when(boardRepository.findBoardByFilters(
                    eq(pageable),
                    eq(filter.getBoardTypeId()),
                    eq(filter.getSearchType()),
                    eq(filter.getSearchKeyword())
            )).thenReturn(page);

            // when
            PageResponseDto<BoardListResponse> result = boardService.searchBoard(filter);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
            assertThat(result.isEmpty()).isFalse();

            verify(boardRepository, times(1))
                    .findBoardByFilters(pageable, filter.getBoardTypeId(), filter.getSearchType(), filter.getSearchKeyword());
        }

        @Test
        @DisplayName("성공 - 게시글 목록 조회 (빈 페이지)")
        void searchBoard_Success_Empty() {
            // given
            BoardFilter filter = BoardFilter.builder()
                    .boardTypeId(1L)
                    .page(1)
                    .size(10)
                    .build();

            Pageable pageable = filter.getPageable();

            // 빈 리스트로 페이지 생성
            Page<Board> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(boardRepository.findBoardByFilters(
                    eq(pageable),
                    eq(filter.getBoardTypeId()),
                    eq(filter.getSearchType()),
                    eq(filter.getSearchKeyword())
            )).thenReturn(emptyPage);

            // when
            PageResponseDto<BoardListResponse> result = boardService.searchBoard(filter);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
            assertThat(result.isEmpty()).isTrue();

            verify(boardRepository, times(1))
                    .findBoardByFilters(pageable, filter.getBoardTypeId(), filter.getSearchType(), filter.getSearchKeyword());
        }
    }

    @Nested
    @DisplayName("getBoard() 테스트")
    class GetBoard {
        @Test
        @DisplayName("성공 - 게시글 상세 조회 (첫 조회)")
        void getBoard_FirstView_IncreasesViewCount() {
            Long boardId = 1L;
            Long userId = 100L;

            Member writer = Member.builder().memberId(200L).build();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용");

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(boardType)
                    .writer(writer)
                    .viewCount(0)
                    .build();

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));

            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.add(anyString(), any())).thenReturn(1L);
            when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(-1L);

            when(fileRepository.findAllByRelatedTypeAndRelatedId(anyString(), eq(boardId)))
                    .thenReturn(List.of());

            BoardResponse result = boardService.getBoard(boardId, userId);

            assertThat(result).isNotNull();
            assertThat(result.getBoardId()).isEqualTo(1L);
            assertThat(result.isOwner()).isFalse();
            assertThat(result.getViewCount()).isEqualTo(1);

            verify(setOperations).add(eq("board:viewed:" + boardId), eq(userId.toString()));
            verify(redisTemplate).expire(anyString(), eq(24L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("성공 - 게시글 상세 조회 (중복 조회)")
        void getBoard_AlreadyViewed_NoIncrease() {
            Long boardId = 1L;
            Long userId = 100L;

            Member writer = Member.builder().memberId(200L).build();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용");

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(boardType)
                    .writer(writer)
                    .viewCount(1)
                    .build();

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(redisTemplate.opsForSet()).thenReturn(setOperations);
            when(setOperations.add(anyString(), any())).thenReturn(1L);

            when(fileRepository.findAllByRelatedTypeAndRelatedId(anyString(), eq(boardId)))
                    .thenReturn(List.of());

            BoardResponse result = boardService.getBoard(boardId, userId);

            assertThat(result.isOwner()).isFalse();
            assertThat(result.getViewCount()).isEqualTo(2);

            verify(setOperations).add(eq("board:viewed:" + boardId), eq(userId.toString()));
            verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
        }

        @Test
        @DisplayName("성공 - 게시글 상세 조회 (작성자 본인 게시글 조회)")
        void getBoard_Owner_NoIncrease() {
            Long boardId = 1L;
            Long userId = 100L;

            Member writer = Member.builder().memberId(100L).build();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용");

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(boardType)
                    .writer(writer)
                    .viewCount(1)
                    .build();

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(fileRepository.findAllByRelatedTypeAndRelatedId(anyString(), eq(boardId)))
                    .thenReturn(List.of());

            BoardResponse result = boardService.getBoard(boardId, userId);

            assertThat(result.isOwner()).isTrue();
            assertThat(result.getViewCount()).isEqualTo(1);

            verify(redisTemplate, never()).opsForSet();
        }

        @Test
        @DisplayName("예외 - 게시글 없음")
        void getBoard_NotFound() {
            Long boardId = 1L;
            Long userId = 100L;

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> boardService.getBoard(boardId, userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
