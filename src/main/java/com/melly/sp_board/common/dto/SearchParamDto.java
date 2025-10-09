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
    private String sortBy;        // 컬럼 이름
    private String sortOrder;    // "asc" 또는 "desc"

    public Pageable getPageable() {
        Sort sort = Sort.by((sortBy == null || sortBy.isBlank())? "createdAt" : sortBy);
        boolean orderFlag = "desc".equalsIgnoreCase(sortOrder);
        sort = orderFlag ? sort.descending() : sort.ascending();
        return PageRequest.of(page - 1, size, sort);
    }
}
