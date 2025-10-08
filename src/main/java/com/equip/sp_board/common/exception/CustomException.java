package com.equip.sp_board.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorType errorType;

    // 기본 메시지 사용
    public CustomException(ErrorType errorType) {
        // super() 생략 시 기본 생성자를 호출하기 때문에 메시지 호출 불가
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    // 커스텀 메시지 사용
    public CustomException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
