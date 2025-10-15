package com.melly.sp_board.common.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SearchParamDto {
    private int page;
    private int size;
    private String sortBy;      // 정렬 기준 컬럼
    private String sortOrder;   // asc or desc

    public Pageable getPageable() {
        // page 최소값 보정
        int safePage = Math.max(page - 1, 0);

        // sortBy 기본값 처리
        String sortField = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;

        // sortOrder 기본값 desc
        Sort.Direction direction = Sort.Direction.DESC;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            direction = Sort.Direction.ASC;
        }

        return PageRequest.of(safePage, size, Sort.by(direction, sortField));
    }
}
