package com.melly.sp_board.board.repository;

import com.melly.sp_board.board.domain.Board;
import com.melly.sp_board.board.domain.BoardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("""
        SELECT b
        FROM Board b
        WHERE  b.status = 'ACTIVE'
            AND (:boardTypeId IS NULL OR b.boardType.boardTypeId = :boardTypeId)
            AND (
              :searchType IS NULL
              OR :searchType = ''
              OR :searchKeyword IS NULL
              OR :searchKeyword = ''
              OR (
                  (:searchType = 'title' AND b.title LIKE %:searchKeyword%)
                  OR (:searchType = 'content' AND b.content LIKE %:searchKeyword%)
                  OR (:searchType = 'writer' AND b.writer.name LIKE %:searchKeyword%)
              )
            )
    """)
    Page<Board> findBoardByFilters(Pageable pageable,
                                   @Param("boardTypeId") Long boardTypeId,
                                   @Param("searchType") String searchType,
                                   @Param("searchKeyword") String searchKeyword);

    // WHERE Board.boardId = ? AND Board.status = ?
    Optional<Board> findByBoardIdAndStatus(Long boardId, BoardStatus boardStatus);
}
