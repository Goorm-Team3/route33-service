package io.goorm.route33.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        logger.info("[Request] " + method + " " + uri +
                (queryString != null ? "?" + queryString : ""));

        filterChain.doFilter(request, response);

        logger.info("[Response] Status: " + response.getStatus());
    }
}
