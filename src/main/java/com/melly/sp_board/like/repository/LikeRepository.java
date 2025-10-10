package com.melly.sp_board.like.repository;

import com.melly.sp_board.like.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    @Query("""
    SELECT l
    FROM Like l
    WHERE l.relatedType = :relatedType
        AND l.relatedId = :relatedId
        AND l.member.memberId = :memberId
    """)
    Optional<Like> findLike(@Param("relatedType") String relatedType,
                            @Param("relatedId") Long relatedId,
                            @Param("memberId") Long memberId);
}
