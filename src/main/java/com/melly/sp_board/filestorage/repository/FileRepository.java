package com.melly.sp_board.filestorage.repository;

import com.melly.sp_board.filestorage.domain.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileMeta, Long> {
}
