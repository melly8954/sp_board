package com.equip.sp_board.common.controller;

import com.equip.sp_board.common.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface ResponseController {
    default <T> ResponseEntity<ResponseDto<T>> makeResponseEntity(String traceId, HttpStatus status, String errorCode, String message, T data) {
        ResponseDto<T> dto = ResponseDto.<T>builder()
                .id(traceId)
                .code(status.value())
                .errorCode(errorCode)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(status).body(dto);
    }
}
