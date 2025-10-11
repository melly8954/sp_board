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
import com.melly.sp_board.filestorage.domain.FileMeta;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            // given
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

            // when
            BoardResponse result = boardService.getBoard(boardId, userId);

            // then
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
            // given
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
            // when
            BoardResponse result = boardService.getBoard(boardId, userId);

            // then
            assertThat(result.isOwner()).isFalse();
            assertThat(result.getViewCount()).isEqualTo(2);

            verify(setOperations).add(eq("board:viewed:" + boardId), eq(userId.toString()));
            verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
        }

        @Test
        @DisplayName("성공 - 게시글 상세 조회 (작성자 본인 게시글 조회)")
        void getBoard_Owner_NoIncrease() {
            // given
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
            // when
            BoardResponse result = boardService.getBoard(boardId, userId);

            // then
            assertThat(result.isOwner()).isTrue();
            assertThat(result.getViewCount()).isEqualTo(1);

            verify(redisTemplate, never()).opsForSet();
        }

        @Test
        @DisplayName("예외 - 게시글 없음")
        void getBoard_NotFound() {
            // given
            Long boardId = 1L;
            Long userId = 100L;

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.getBoard(boardId, userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateBoard() 단위 테스트")
    class UpdateBoard {
        @Test
        @DisplayName("성공 - 게시글 수정 (파일 없음)")
        void updateBoard_Success_NoFiles() {
            // given
            Long boardId = 1L;
            Long currentUserId = 100L;

            Member writer = Member.builder().memberId(100L).build();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용");

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(boardType)
                    .writer(writer)
                    .title("기존 제목")
                    .content("기존 내용")
                    .build();

            UpdateBoardRequest dto = UpdateBoardRequest.builder()
                    .title("수정 제목")
                    .content("수정 내용")
                    .build();

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(fileRepository.findAllByRelatedTypeAndRelatedId(anyString(), eq(boardId)))
                    .thenReturn(List.of());

            // when
            UpdateBoardResponse result = boardService.updateBoard(boardId, dto, null, currentUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("수정 제목");
            assertThat(result.getContent()).isEqualTo("수정 내용");

            verify(fileRepository, never()).delete(any());
            verify(fileService, never()).deleteFile(anyString(), anyString());
            verify(fileService, never()).saveFiles(any(), anyString());
        }

        @Test
        @DisplayName("성공 - 게시글 수정 (기존 파일 삭제)")
        void updateBoard_RemoveFilesOnly() {
            // given
            Long boardId = 1L;
            Long userId = 100L;

            Member writer = Member.builder().memberId(100L).build();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용");

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(boardType)
                    .writer(writer)
                    .build();
            UpdateBoardRequest dto = UpdateBoardRequest.builder()
                    .removeFileIds(List.of(1L, 2L))
                    .build();

            FileMeta file1 = FileMeta.builder().fileId(1L).filePath("url1").build();
            FileMeta file2 = FileMeta.builder().fileId(2L).filePath("url2").build();

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(fileRepository.findAllById(dto.getRemoveFileIds())).thenReturn(List.of(file1, file2));

            // when
            UpdateBoardResponse result = boardService.updateBoard(boardId, dto, null, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFiles()).isEmpty();

            verify(fileRepository).delete(file1);
            verify(fileRepository).delete(file2);
            verify(fileService).deleteFile("url1", "board_공지");
            verify(fileService).deleteFile("url2", "board_공지");
        }

        @Test
        @DisplayName("성공 - 게시글 수정 (새 파일 추가)")
        void updateBoard_AddNewFilesOnly() throws IOException {
            // given
            Long boardId = 1L;
            Long userId = 100L;

            Member writer = Member.builder().memberId(100L).build();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용");

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(boardType)
                    .writer(writer)
                    .build();
            UpdateBoardRequest dto = UpdateBoardRequest.builder().build();

            // 실제 MultipartFile 객체 대신 Mock 객체 사용
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("file1.txt");
            when(mockFile.getContentType()).thenReturn("text/plain");
            when(mockFile.getSize()).thenReturn(100L);

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(fileService.saveFiles(List.of(mockFile), "board_공지")).thenReturn(List.of("http://files/board_공지/file1.txt"));
            when(fileRepository.countByRelatedTypeAndRelatedId("board_공지", boardId)).thenReturn(0);
            when(fileRepository.findAllByRelatedTypeAndRelatedId("board_공지", boardId))
                    .thenReturn(List.of(FileMeta.builder()
                            .relatedType("board_공지")
                            .relatedId(boardId)
                            .originalName("file1.txt")
                            .uniqueName("file1.txt")
                            .fileOrder(0)
                            .filePath("http://files/board_공지/file1.txt")
                            .fileType("text/plain")
                            .fileSize(100L)
                            .build()));

            // when
            UpdateBoardResponse result = boardService.updateBoard(boardId, dto, List.of(mockFile), userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFiles()).hasSize(1);
            assertThat(result.getFiles().get(0).getOriginalName()).isEqualTo("file1.txt");
            assertThat(result.getFiles().get(0).getFilePath()).isEqualTo("http://files/board_공지/file1.txt");

            verify(fileService).saveFiles(List.of(mockFile), "board_공지");
            verify(fileRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("성공 - 게시글 수정 (기존 파일 삭제 + 새 파일 추가)")
        void updateBoard_RemoveAndAddFiles() throws IOException {
            // given
            Long boardId = 1L;
            Long userId = 100L;

            Member writer = Member.builder().memberId(100L).build();
            BoardType boardType = new BoardType(1L, "공지", "공지 게시판 전용");

            Board board = Board.builder()
                    .boardId(boardId)
                    .boardType(boardType)
                    .writer(writer)
                    .build();
            UpdateBoardRequest dto = UpdateBoardRequest.builder()
                    .removeFileIds(List.of(1L))
                    .build();

            FileMeta removeFile = FileMeta.builder().fileId(1L).filePath("http://files/board_공지/file1.txt").build();

            MultipartFile newFile = mock(MultipartFile.class);
            when(newFile.getOriginalFilename()).thenReturn("file2.txt");
            when(newFile.getContentType()).thenReturn("text/plain");
            when(newFile.getSize()).thenReturn(200L);

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(fileRepository.findAllById(dto.getRemoveFileIds())).thenReturn(List.of(removeFile));
            when(fileService.saveFiles(List.of(newFile), "board_공지")).thenReturn(List.of("http://files/board_공지/file2.txt"));
            when(fileRepository.countByRelatedTypeAndRelatedId("board_공지", boardId)).thenReturn(0);
            when(fileRepository.findAllByRelatedTypeAndRelatedId("board_공지", boardId))
                    .thenReturn(List.of(FileMeta.builder()
                            .relatedType("board_공지")
                            .relatedId(boardId)
                            .originalName("file2.txt")
                            .uniqueName("file2.txt")
                            .fileOrder(0)
                            .filePath("http://files/board_공지/file2.txt")
                            .fileType("text/plain")
                            .fileSize(200L)
                            .build()));

            // when
            UpdateBoardResponse result = boardService.updateBoard(boardId, dto, List.of(newFile), userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFiles()).hasSize(1);
            assertThat(result.getFiles().get(0).getOriginalName()).isEqualTo("file2.txt");
            assertThat(result.getFiles().get(0).getFilePath()).isEqualTo("http://files/board_공지/file2.txt");

            verify(fileRepository).delete(removeFile);
            verify(fileService).deleteFile("http://files/board_공지/file1.txt", "board_공지");
            verify(fileService).saveFiles(List.of(newFile), "board_공지");
            verify(fileRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("예외 - 게시글 없음")
        void updateBoard_NotFound() {
            // given
            Long boardId = 1L;
            Long userId = 100L;

            UpdateBoardRequest dto = UpdateBoardRequest.builder().build();

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.updateBoard(boardId, dto, null, userId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("해당 게시글은 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("softDeleteBoard() 단위 테스트")
    class SoftDeleteBoard {
        @Test
        @DisplayName("성공 - 게시글 소프트 삭제")
        void softDeleteBoard_Success() {
            // given
            Long boardId = 1L;
            Long currentUserId = 100L;

            Member member = Member.builder().memberId(currentUserId).build();

            Board board = Board.builder()
                    .boardId(boardId)
                    .writer(member)
                    .status(BoardStatus.ACTIVE)
                    .build();

            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(memberRepository.findById(currentUserId))
                    .thenReturn(Optional.of(member));

            // when
            boardService.softDeleteBoard(boardId, currentUserId);

            // then
            assertThat(board.getStatus()).isEqualTo(BoardStatus.DELETED);
            verify(boardRepository, times(1)).findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE);
            verify(memberRepository, times(1)).findById(currentUserId);
        }

        @Test
        @DisplayName("예외 - 게시글 없음")
        void softDeleteBoard_BoardNotFound() {
            // given
            when(boardRepository.findByBoardIdAndStatus(1L, BoardStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.softDeleteBoard(1L, 100L))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("예외 - 회원 없음")
        void softDeleteBoard_MemberNotFound() {
            // given
            Board board = Board.builder().boardId(1L).build();
            when(boardRepository.findByBoardIdAndStatus(1L, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(memberRepository.findById(100L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.softDeleteBoard(1L, 100L))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
