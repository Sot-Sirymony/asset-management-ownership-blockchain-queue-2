package com.hrd.asset_holder_api.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//public class SwaggerAccessLoggingFilter {
//}
@Component
@Slf4j
public class SwaggerAccessLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")) {

            log.info("Swagger accessed | method={} | uri={} | ip={}",
                    request.getMethod(),
                    uri,
                    request.getRemoteAddr());
        }

        filterChain.doFilter(request, response);
    }
}