package com.melly.sp_board.comment.service;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.board.repository.BoardRepository;
import com.melly.sp_board.comment.domain.Comment;
import com.melly.sp_board.comment.domain.CommentStatus;
import com.melly.sp_board.comment.dto.CreateCommentRequest;
import com.melly.sp_board.comment.dto.CreateCommentResponse;
import com.melly.sp_board.comment.repository.CommentRepository;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public CreateCommentResponse createComment(CreateCommentRequest dto, Long currentUserId) {
        Member writer = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 사용자는 존재하지 않습니다."));
        Board board = boardRepository.findByBoardIdAndStatusNot(dto.getBoardId(), BoardStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 게시글은 존재하지 않습니다."));

        // parentCommentId가 있으면 찾아오기
        Comment parent = null;
        if (dto.getParentCommentId() != null) {
            parent = commentRepository.findByCommentIdAndStatusNot(dto.getParentCommentId(), CommentStatus.DELETED)
                    .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 댓글은 존재하지 않습니다."));
        }

        Comment comment = Comment.builder()
                .board(board)
                .writer(writer)
                .parent(parent)
                .content(dto.getContent())
                .likeCount(0)
                .status(CommentStatus.ACTIVE)
                .build();
        commentRepository.save(comment);

        return CreateCommentResponse.builder()
                .commentId(comment.getCommentId())
                .boardId(comment.getBoard().getBoardId())
                .writerId(comment.getWriter().getMemberId())
                .content(comment.getContent())
                .build();
    }
}
