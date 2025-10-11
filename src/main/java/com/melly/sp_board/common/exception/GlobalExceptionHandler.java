package com.melly.sp_board.common.exception;


import com.melly.sp_board.common.controller.ResponseController;
import com.melly.sp_board.common.dto.ResponseDto;
import com.melly.sp_board.common.trace.RequestTraceIdFilter;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler implements ResponseController {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
        String traceId = RequestTraceIdFilter.getTraceId();
        ErrorType errorType = e.getErrorType();
        String message = e.getMessage();
        log.error("TraceId: {}, 비즈니스 로직 예외 발생 - Code: {}, Message: {}",
                traceId, errorType.getErrorCode(), message);

        return makeResponseEntity(
                traceId,
                errorType.getStatus(),
                errorType.getErrorCode(),
                message,
                null
        );
    }

    @ExceptionHandler({RedisConnectionFailureException.class, RedisCommandTimeoutException.class})
    public ResponseEntity<ResponseDto<Void>> handleRedisConnection(Exception e) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.error("TraceId: {}, Redis 연결 실패", traceId, e);

        return makeResponseEntity(
                traceId,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal_server_error",
                "Redis 연결 실패",
                null
        );
    }

    @ExceptionHandler(RedisSystemException.class)
    public ResponseEntity<ResponseDto<Void>> handleRedisSystem(Exception e) {
        String traceId = RequestTraceIdFilter.getTraceId();
        log.error("TraceId: {}, Redis 명령 실행 실패", traceId, e);

        return makeResponseEntity(
                traceId,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal_server_error",
                "Redis 명령 실행 실패",
                null
        );
    }

}
