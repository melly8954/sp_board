package com.melly.sp_board.board.repository;

import com.melly.sp_board.board.domain.BoardType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardTypeRepository extends JpaRepository<BoardType, Long> {
}
