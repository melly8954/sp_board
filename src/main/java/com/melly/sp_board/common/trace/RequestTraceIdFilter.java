package com.melly.sp_board.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestTraceIdFilter extends OncePerRequestFilter {
    // 요청별 Trace ID를 저장할 ThreadLocal
    // ThreadLocal 사용하면 같은 쓰레드 내 어디서든 접근 가능
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    // 외부에서 현재 요청의 Trace ID를 조회할 수 있는 메서드
    public static String getTraceId() {
        return TRACE_ID.get();
    }

    // 사용 후 ThreadLocal 제거 (메모리 누수 방지)
    public static void clear() {
        TRACE_ID.remove();
    }

    /**
     * 실제 요청 처리 로직
     * 모든 요청에서 실행됨 (OncePerRequestFilter 덕분에 한 요청당 한 번만 호출)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 요청마다 고유한 UUID를 생성하고 ThreadLocal 저장
            TRACE_ID.set(UUID.randomUUID().toString());

            // 다음 필터 또는 컨트롤러로 요청 전달
            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리 종료 후 ThreadLocal 제거 → 메모리 누수 방지
            TRACE_ID.remove();
        }
    }
}
