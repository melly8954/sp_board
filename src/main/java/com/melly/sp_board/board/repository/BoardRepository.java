package com.melly.sp_board.board.repository;

import com.melly.sp_board.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
