package com.melly.sp_board.filestorage.repository;

import com.melly.sp_board.filestorage.domain.FileMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileMeta, Long> {
    Optional<FileMeta> findByRelatedTypeAndRelatedId(String member, Long memberId);
    List<FileMeta> findAllByRelatedTypeAndRelatedId(String relatedType, Long boardId);

    int countByRelatedTypeAndRelatedId(String relatedType, Long boardId);
}
