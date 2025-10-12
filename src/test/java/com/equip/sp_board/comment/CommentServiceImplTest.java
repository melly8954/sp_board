package com.equip.sp_board.comment;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.board.repository.BoardRepository;
import com.melly.sp_board.comment.domain.Comment;
import com.melly.sp_board.comment.domain.CommentStatus;
import com.melly.sp_board.comment.dto.CreateCommentRequest;
import com.melly.sp_board.comment.repository.CommentRepository;
import com.melly.sp_board.comment.service.CommentServiceImpl;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl 단위 테스트")
public class CommentServiceImplTest {
    @Mock CommentRepository commentRepository;
    @Mock MemberRepository memberRepository;
    @Mock BoardRepository boardRepository;

    @InjectMocks CommentServiceImpl commentServiceImpl;

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
            commentServiceImpl.createComment(dto, memberId);

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
            commentServiceImpl.createComment(dto, memberId);

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
            assertThatThrownBy(() -> commentServiceImpl.createComment(dto, memberId))
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
            assertThatThrownBy(() -> commentServiceImpl.createComment(dto, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(memberRepository).findById(memberId);
            verify(boardRepository).findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE);
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }
}
