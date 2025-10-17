package com.melly.sp_board.like.service;

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
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    @Override
    @Transactional
    public String toggleBoardLike(Long boardId, Long currentUserId) {
        Member member = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 사용자는 존재하지 않습니다."));
        Board board = boardRepository.findByBoardIdAndStatus(boardId, BoardStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시글은 존재하지 않습니다."));

        if (board.getWriter().getMemberId().equals(currentUserId)) {
            throw new CustomException(ErrorType.FORBIDDEN, "자기 게시글에는 좋아요를 누를 수 없습니다.");
        }

        String relatedType = "board";
        Optional<Like> existing = likeRepository.findLike(relatedType, boardId, currentUserId);

        String message = "";
        if (existing.isPresent()) {
            // 이미 좋아요 → 취소
            likeRepository.delete(existing.get());
            board.decreaseLikeCount();
            message = "좋아요 취소 성공";
        } else {
            // 좋아요 추가
            Like like = Like.builder()
                    .member(member)
                    .relatedType(relatedType)
                    .relatedId(boardId)
                    .createdAt(LocalDateTime.now())
                    .build();
            likeRepository.save(like);
            board.increaseLikeCount();
            message = "좋아요 등록 성공";
        }
        return message;
    }

    @Override
    @Transactional
    public String toggleCommentLike(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 댓글은 존재하지 않습니다."));
        Member member = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 회원은 존재하지 않습니다."));

        if (comment.getWriter().getMemberId().equals(currentUserId)) {
            throw new CustomException(ErrorType.FORBIDDEN, "자기 댓글에는 좋아요를 누를 수 없습니다.");
        }
        
        String relatedType = "comment";
        Optional<Like> existing = likeRepository.findLike(relatedType, commentId, currentUserId);

        String message = "";
        if (existing.isPresent()) {
            // 이미 좋아요 → 취소
            likeRepository.delete(existing.get());
            comment.decreaseLikeCount();
            message = "좋아요 취소 성공";
        } else {
            // 좋아요 추가
            Like like = Like.builder()
                    .member(member)
                    .relatedType(relatedType)
                    .relatedId(commentId)
                    .createdAt(LocalDateTime.now())
                    .build();
            likeRepository.save(like);
            comment.increaseLikeCount();
            message = "좋아요 등록 성공";
        }
        return message;
    }
}
