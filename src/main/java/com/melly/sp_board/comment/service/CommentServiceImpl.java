package com.melly.sp_board.comment.service;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import com.melly.sp_board.board.repository.BoardRepository;
import com.melly.sp_board.comment.domain.Comment;
import com.melly.sp_board.comment.domain.CommentStatus;
import com.melly.sp_board.comment.dto.*;
import com.melly.sp_board.comment.repository.CommentRepository;
import com.melly.sp_board.common.dto.PageResponseDto;
import com.melly.sp_board.common.exception.CustomException;
import com.melly.sp_board.common.exception.ErrorType;
import com.melly.sp_board.member.domain.Member;
import com.melly.sp_board.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<CommentListResponse> getCommentList(CommentFilter filter, Long currentUserId) {
        Pageable pageable = filter.getPageable();

        Page<Comment> page = commentRepository.findParentComments(pageable, filter.getBoardId(), CommentStatus.ACTIVE);
        List<Comment> parentComments = page.getContent();

        // 부모 댓글 ID 추출
        List<Long> parentIds = parentComments.stream()
                .map(Comment::getCommentId)
                .toList();

        // 부모 댓글의 모든 자식 댓글 조회
        List<Comment> childComments = commentRepository.findByParentCommentIdInAndStatus(parentIds, CommentStatus.ACTIVE);

        // 부모 ID 기준으로 그룹화
        Map<Long, List<Comment>> childMap = childComments.stream()
                .collect(Collectors.groupingBy(c -> c.getParent().getCommentId()));

        // 부모 댓글 기준 트리 생성
        List<CommentListResponse> roots = parentComments.stream()
                .map(parent -> buildTree(parent, currentUserId, childMap))
                .toList();

        // 페이지 DTO 반환
        return PageResponseDto.<CommentListResponse>builder()
                .content(roots)
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public UpdateCommentResponse updateComment(Long commentId, UpdateCommentRequest dto, Long currentUserId) {
        Comment comment = commentRepository.findByCommentIdAndStatusNot(commentId, CommentStatus.DELETED)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 댓글은 존재하지 않습니다."));
        Member currentUser = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND, "해당 회원은 존재하지 않습니다."));

        String oldContent = comment.getContent();

        comment.updateComment(currentUser, dto);
        commentRepository.flush();

        return UpdateCommentResponse.builder()
                .commentId(comment.getCommentId())
                .oldContent(oldContent)
                .newContent(comment.getContent())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    // 댓글 트리 구조 헬퍼 메서드
    private CommentListResponse buildTree(Comment parent, Long currentUserId, Map<Long, List<Comment>> childMap) {
        List<CommentListResponse> children = childMap.getOrDefault(parent.getCommentId(), List.of())
                .stream()
                .map(child -> buildTree(child, currentUserId, childMap))
                .toList();

        return CommentListResponse.builder()
                .commentId(parent.getCommentId())
                .writerId(parent.getWriter().getMemberId())
                .writerName(parent.getWriter().getName())
                .isOwner(parent.getWriter().getMemberId().equals(currentUserId))
                .content(parent.getContent())
                .likeCount(parent.getLikeCount())
                .createdAt(parent.getCreatedAt())
                .children(children)
                .build();
    }
}
