package com.melly.sp_board.member.repository;

import com.melly.sp_board.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);

    Optional<Member> findByUsername(String username);
}
