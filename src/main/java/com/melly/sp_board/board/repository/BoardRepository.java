package com.melly.sp_board.board.repository;

import com.melly.sp_board.board.domain.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("""
        SELECT b
        FROM Board b
        WHERE (:boardTypeId IS NULL OR b.boardType.boardTypeId = :boardTypeId)
            AND b.status = 'ACTIVE'
    """)
    Page<Board> findBoardByFilters(Pageable pageable, @Param("boardTypeId") Long boardTypeId);
}
