package com.equip.sp_board.like;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.board.repository.BoardRepository;
import com.melly.sp_board.comment.domain.Comment;
import com.melly.sp_board.comment.domain.CommentStatus;
import com.melly.sp_board.comment.repository.CommentRepository;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.like.domain.Like;
import com.melly.sp_board.like.repository.LikeRepository;
import com.melly.sp_board.like.service.LikeServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeServiceImpl 단위 테스트")
public class LikeServiceImplTest {
    @Mock MemberRepository memberRepository;
    @Mock BoardRepository boardRepository;
    @Mock CommentRepository commentRepository;
    @Mock LikeRepository likeRepository;

    @InjectMocks
    LikeServiceImpl likeServiceImpl;

    @Nested
    @DisplayName("toggleBoardLike() 메서드 테스트")
    class toggleBoardLike {
        @Test
        @DisplayName("성공 - 게시글 좋아요 등록")
        void toggleBoardLike_AddLike() {
            Long boardId = 1L;
            Long memberId = 10L;

            Member member = Member.builder().memberId(memberId).build();
            Member writer = Member.builder().memberId(20L).build();

            Board board = Board.builder()
                    .boardId(boardId)
                    .writer(writer)
                    .likeCount(0)
                    .status(BoardStatus.ACTIVE)
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE)).thenReturn(Optional.of(board));
            when(likeRepository.findLike("board", boardId, memberId)).thenReturn(Optional.empty());

            String result = likeServiceImpl.toggleBoardLike(boardId, memberId);

            assertThat(result).isEqualTo("좋아요 등록 성공");
            assertThat(board.getLikeCount()).isEqualTo(1);

            verify(likeRepository).save(any());
            verify(boardRepository, never()).save(any()); // board 자체는 save 호출 없음 (엔티티 변경 후 flush/트랜잭션 커밋)
        }

        @Test
        @DisplayName("성공 - 게시글 좋아요 취소")
        void toggleBoardLike_RemoveLike() {
            Long boardId = 1L;
            Long memberId = 10L;

            Member member = Member.builder().memberId(memberId).build();
            Member writer = Member.builder().memberId(20L).build();

            Board board = Board.builder()
                    .boardId(boardId)
                    .writer(writer)
                    .likeCount(1)
                    .status(BoardStatus.ACTIVE)
                    .build();
            Like existingLike = Like.builder()
                    .likeId(100L)
                    .member(member)
                    .relatedType("board")
                    .relatedId(boardId)
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE)).thenReturn(Optional.of(board));
            when(likeRepository.findLike("board", boardId, memberId)).thenReturn(Optional.of(existingLike));

            String result = likeServiceImpl.toggleBoardLike(boardId, memberId);

            assertThat(result).isEqualTo("좋아요 취소 성공");
            assertThat(board.getLikeCount()).isEqualTo(0);

            verify(likeRepository).delete(existingLike);
        }

        @Test
        @DisplayName("예외 - 회원 없음")
        void toggleBoardLike_NoMember() {
            // given
            Long boardId = 1L;
            Long memberId = 10L;

            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // given & then
            assertThatThrownBy(() -> likeServiceImpl.toggleBoardLike(boardId, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(memberRepository).findById(memberId);
            verify(boardRepository, never()).findByBoardIdAndStatus(anyLong(), any());
            verify(likeRepository, never()).save(any());
            verify(likeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("예외 - 게시글 없음")
        void toggleBoardLike_NoBoard() {
            // given
            Long boardId = 1L;
            Long memberId = 10L;
            Member member = Member.builder().memberId(memberId).build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE)).thenReturn(Optional.empty());

            // given & then
            assertThatThrownBy(() -> likeServiceImpl.toggleBoardLike(boardId, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(memberRepository).findById(memberId);
            verify(boardRepository).findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE);
            verify(likeRepository, never()).save(any());
            verify(likeRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("toggleCommentLike() 메서드 테스트")
    class toggleCommentLike {
        @Test
        @DisplayName("성공 - 댓글 좋아요 등록")
        void toggleCommentLike_AddLike() {
            // given
            Long commentId = 1L;
            Long memberId = 10L;

            Member member = Member.builder().memberId(memberId).build();
            Member writer = Member.builder().memberId(20L).build();

            Comment comment = Comment.builder()
                    .commentId(commentId)
                    .writer(writer)
                    .likeCount(0)
                    .status(CommentStatus.ACTIVE)
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE)).thenReturn(Optional.of(comment));
            when(likeRepository.findLike("comment", commentId, memberId)).thenReturn(Optional.empty());

            // when
            String result = likeServiceImpl.toggleCommentLike(commentId, memberId);

            // then
            assertThat(result).isEqualTo("좋아요 등록 성공");
            assertThat(comment.getLikeCount()).isEqualTo(1);

            verify(likeRepository).save(any());
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공 - 댓글 좋아요 취소")
        void toggleCommentLike_RemoveLike() {
            // given
            Long commentId = 1L;
            Long memberId = 10L;

            Member member = Member.builder().memberId(memberId).build();
            Member writer = Member.builder().memberId(20L).build();

            Comment comment = Comment.builder()
                    .commentId(commentId)
                    .writer(writer)
                    .likeCount(1)
                    .status(CommentStatus.ACTIVE)
                    .build();
            Like existingLike = Like.builder()
                    .likeId(100L)
                    .member(member)
                    .relatedType("comment")
                    .relatedId(commentId)
                    .build();

            when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE)).thenReturn(Optional.of(comment));
            when(likeRepository.findLike("comment", commentId, memberId)).thenReturn(Optional.of(existingLike));

            // when
            String result = likeServiceImpl.toggleCommentLike(commentId, memberId);

            // then
            assertThat(result).isEqualTo("좋아요 취소 성공");
            assertThat(comment.getLikeCount()).isEqualTo(0);

            verify(likeRepository).delete(existingLike);
        }

        @Test
        @DisplayName("예외 - 댓글 없음")
        void toggleCommentLike_NoComment() {
            // given
            Long commentId = 1L;
            Long memberId = 10L;

            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> likeServiceImpl.toggleCommentLike(commentId, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(commentRepository).findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE);
            verify(memberRepository, never()).findById(anyLong());
            verify(likeRepository, never()).save(any());
            verify(likeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("예외 - 회원 없음")
        void toggleCommentLike_NoMember() {
            // given
            Long commentId = 1L;
            Long memberId = 10L;
            Comment comment = Comment.builder().commentId(commentId).status(CommentStatus.ACTIVE).build();

            // 댓글은 존재
            when(commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE))
                    .thenReturn(Optional.of(comment));

            // 회원은 존재하지 않음
            when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> likeServiceImpl.toggleCommentLike(commentId, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            verify(commentRepository).findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE);
            verify(memberRepository).findById(memberId);
            verify(likeRepository, never()).save(any());
            verify(likeRepository, never()).delete(any());
        }
    }
}
