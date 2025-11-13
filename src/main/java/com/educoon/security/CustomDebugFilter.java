package com.educoon.security; // (패키지는 맞게 수정하세요)

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class CustomDebugFilter extends GenericFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // [프린트문]
        log.warn("=============================================================");
        log.warn("[DEBUG FILTER] 요청 진입: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());
        log.warn("=============================================================");

        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }
}
