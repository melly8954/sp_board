package com.equip.sp_board.comment;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.board.repository.BoardRepository;
import com.melly.sp_board.comment.domain.Comment;
import com.melly.sp_board.comment.domain.CommentStatus;
import com.melly.sp_board.comment.dto.*;
import com.melly.sp_board.comment.repository.CommentRepository;
import com.melly.sp_board.comment.service.CommentServiceImpl;
import com.melly.sp_board.common.dto.PageResponseDto;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
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
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl 단위 테스트")
public class CommentServiceImplTest {
    @Mock CommentRepository commentRepository;
    @Mock MemberRepository memberRepository;
    @Mock BoardRepository boardRepository;

    @InjectMocks CommentServiceImpl commentService;

    @Nested
    @DisplayName("createComment() 메서드 테스트")
    class createComment {
        @Test
        @DisplayName("성공 - 댓글 작성 (상위 댓글 없음)")
        void createComment_Success_NoParent() {
            // given
            Long memberId = 1L;
            Long boardId = 10L;

            Member writer = Member.builder().memberId(memberId).build();
            Board board = Board.builder().boardId(boardId).status(BoardStatus.ACTIVE).build();

            CreateCommentRequest dto = CreateCommentRequest.builder()
                    .boardId(boardId)
                    .content("댓글 작성 테스트")
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(writer));
            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));

            // when
            commentService.createComment(dto, memberId);

            // then
            verify(memberRepository).findById(memberId);
            verify(boardRepository).findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("성공 - 댓글 작성 (상위 댓글 존재)")
        void createComment_Success_WithParent() {
            // given
            Long memberId = 1L;
            Long boardId = 10L;
            Long parentId = 100L;

            Member writer = Member.builder().memberId(memberId).build();
            Board board = Board.builder().boardId(boardId).status(BoardStatus.ACTIVE).build();
            Comment parent = Comment.builder().commentId(parentId).status(CommentStatus.ACTIVE).build();

            CreateCommentRequest dto = CreateCommentRequest.builder()
                    .boardId(boardId)
                    .parentCommentId(parentId)
                    .content("댓글 답변 테스트")
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(writer));
            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.of(board));
            when(commentRepository.findByCommentIdAndStatus(parentId, CommentStatus.ACTIVE))
                    .thenReturn(Optional.of(parent));

            // when
            commentService.createComment(dto, memberId);

            // then
            verify(memberRepository).findById(memberId);
            verify(boardRepository).findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE);
            verify(commentRepository).findByCommentIdAndStatus(parentId, CommentStatus.ACTIVE);
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("예외 - 작성자가 존재하지 않는 경우")
        void createComment_Fail_NoMember() {
            // given
            Long memberId = 1L;
            Long boardId = 10L;

            CreateCommentRequest dto = CreateCommentRequest.builder()
                    .boardId(boardId)
                    .content("테스트 댓글")
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(dto, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(memberRepository).findById(memberId);
            verify(boardRepository, never()).findByBoardIdAndStatus(anyLong(), any());
            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("예외 - 게시글이 존재하지 않는 경우")
        void createComment_Fail_NoBoard() {
            // given
            Long memberId = 1L;
            Long boardId = 10L;

            Member writer = Member.builder().memberId(memberId).build();
            CreateCommentRequest dto = CreateCommentRequest.builder()
                    .boardId(boardId)
                    .content("테스트 댓글")
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(writer));
            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(dto, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(memberRepository).findById(memberId);
            verify(boardRepository).findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE);
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("getCommentList() 메서드 테스트")
    class getCommentList {
        @Test
        @DisplayName("성공 - 댓글 목록 조회 (상위 댓글만 존재")
        void getCommentList_ParentsOnly() {
            // given
            Long boardId = 1L;
            Long currentUserId = 10L;

            CommentFilter filter = CommentFilter.builder()
                    .page(1)
                    .size(1)
                    .boardId(boardId)
                    .build();

            Pageable pageable = filter.getPageable();

            // 테스트용 작성자
            Member writer = Member.builder().memberId(1L).build();

            // 부모 댓글
            Comment parent1 = Comment.builder().commentId(100L).writer(writer).build();
            List<Comment> parents = List.of(parent1);

            Page<Comment> parentPage = mock(Page.class);
            when(parentPage.getContent()).thenReturn(parents);
            when(parentPage.getNumber()).thenReturn(0);
            when(parentPage.getSize()).thenReturn(10);
            when(parentPage.getTotalElements()).thenReturn(1L);
            when(parentPage.getTotalPages()).thenReturn(1);
            when(parentPage.getNumberOfElements()).thenReturn(1);
            when(parentPage.isFirst()).thenReturn(true);
            when(parentPage.isLast()).thenReturn(true);
            when(parentPage.isEmpty()).thenReturn(false);

            when(commentRepository.findParentComments(pageable, boardId, CommentStatus.ACTIVE)).thenReturn(parentPage);
            when(commentRepository.findByParentCommentIdInAndStatus(List.of(100L), CommentStatus.ACTIVE))
                    .thenReturn(List.of());

            // when
            PageResponseDto<CommentListResponse> result = commentService.getCommentList(filter, currentUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getChildren()).isEmpty();

            verify(commentRepository).findParentComments(pageable, boardId, CommentStatus.ACTIVE);
            verify(commentRepository).findByParentCommentIdInAndStatus(List.of(100L), CommentStatus.ACTIVE);
        }

        @Test
        @DisplayName("성공 - 댓글 목록 조회 (상위,하위 댓글 모두 존재")
        void getCommentList_Success() {
            // given
            Long boardId = 1L;
            Long currentUserId = 10L;

            CommentFilter filter = CommentFilter.builder()
                    .page(1)
                    .size(1)
                    .boardId(boardId)
                    .build();

            Pageable pageable = filter.getPageable();

            // 테스트용 작성자
            Member writer = Member.builder().memberId(1L).build();

            // 부모 댓글
            Comment parent1 = Comment.builder().commentId(100L).writer(writer).build();
            Comment parent2 = Comment.builder().commentId(101L).writer(writer).build();
            List<Comment> parents = List.of(parent1, parent2);

            // 자식 댓글
            Comment child1 = Comment.builder().commentId(200L).parent(parent1).writer(writer).build();
            Comment child2 = Comment.builder().commentId(201L).parent(parent2).writer(writer).build();
            List<Comment> children = List.of(child1, child2);

            Page<Comment> parentPage = mock(Page.class);
            when(parentPage.getContent()).thenReturn(parents);
            when(parentPage.getNumber()).thenReturn(0);
            when(parentPage.getSize()).thenReturn(10);
            when(parentPage.getTotalElements()).thenReturn(2L);
            when(parentPage.getTotalPages()).thenReturn(1);
            when(parentPage.getNumberOfElements()).thenReturn(2);
            when(parentPage.isFirst()).thenReturn(true);
            when(parentPage.isLast()).thenReturn(true);
            when(parentPage.isEmpty()).thenReturn(false);

            when(commentRepository.findParentComments(pageable, boardId, CommentStatus.ACTIVE)).thenReturn(parentPage);
            when(commentRepository.findByParentCommentIdInAndStatus(List.of(100L, 101L), CommentStatus.ACTIVE))
                    .thenReturn(children);

            // when
            PageResponseDto<CommentListResponse> result = commentService.getCommentList(filter, currentUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPage()).isEqualTo(1);

            verify(commentRepository).findParentComments(pageable, boardId, CommentStatus.ACTIVE);
            verify(commentRepository).findByParentCommentIdInAndStatus(List.of(100L, 101L), CommentStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("updateComment() 메서드 테스트")
    class updateComment {
        @Test
        @DisplayName("성공 - 댓글 수정")
        void updateComment_Success() {
            // given
            Long commentId = 100L;
            Long currentUserId = 1L;

            Comment comment = mock(Comment.class);
            UpdateCommentRequest dto = UpdateCommentRequest.builder()
                    .content("수정된 댓글 내용")
                    .build();

            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE))
                    .thenReturn(Optional.of(comment));

            // when
            commentService.updateComment(commentId, dto, currentUserId);

            // then
            verify(commentRepository).findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE);
            verify(comment).updateComment(dto, currentUserId);
            verify(commentRepository).flush();
        }

        @Test
        @DisplayName("예외 - 댓글이 존재하지 않는 경우")
        void updateComment_Fail_NotFound() {
            // given
            Long commentId = 100L;
            Long currentUserId = 1L;
            UpdateCommentRequest dto = UpdateCommentRequest.builder().content("내용").build();

            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(commentId, dto, currentUserId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(commentRepository, never()).flush();
        }
    }

    @Nested
    @DisplayName("softDeleteComment() 메서드 테스트")
    class softDeleteComment {
        @Test
        @DisplayName("성공 - 댓글 삭제 (soft delete)")
        void softDeleteComment_Success() {
            Long commentId = 100L;
            Long currentUserId = 1L;

            Comment comment = mock(Comment.class);
            Member currentUser = mock(Member.class);

            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE))
                    .thenReturn(Optional.of(comment));
            when(memberRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));

            commentService.softDeleteComment(commentId, currentUserId);

            verify(commentRepository).findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE);
            verify(memberRepository).findById(currentUserId);
            verify(comment).softDeleteComment(currentUser);
        }

        @Test
        @DisplayName("예외 - 댓글 삭제 시 댓글 없음")
        void softDeleteComment_Fail_NoComment() {
            Long commentId = 100L;
            Long currentUserId = 1L;

            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.softDeleteComment(commentId, currentUserId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(commentRepository).findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE);
            verify(memberRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("예외 - 댓글 삭제 시 회원 없음")
        void softDeleteComment_Fail_NoMember() {
            Long commentId = 100L;
            Long currentUserId = 1L;

            Comment comment = mock(Comment.class);
            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE))
                    .thenReturn(Optional.of(comment));
            when(memberRepository.findById(currentUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.softDeleteComment(commentId, currentUserId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(commentRepository).findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE);
            verify(memberRepository).findById(currentUserId);
            verify(comment, never()).softDeleteComment(any());
        }
    }
}
