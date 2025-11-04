package com.example.demo.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class RequestHeaderFilter extends OncePerRequestFilter {
    private final List<String> unwantedHeaders = List.of(
            "cookie",
            "postman-token",
            "content-length",
            "host",
            "user-agent",
            "accept",
            "accept-encoding",
            "connection"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> headers = new ArrayList<>();
                Enumeration<String> requestHeaders = request.getHeaderNames();
                while (requestHeaders.hasMoreElements()) {
                    String header = requestHeaders.nextElement();
                    if (!unwantedHeaders.contains(header)) {
                        headers.add(header);
                    }
                }
                return Collections.enumeration(headers);
            }

            @Override
            public String getHeader(String name) {
                Enumeration<String> headers = getHeaderNames();
                List<String> listHeaders = Collections.list(headers);
                if (listHeaders.contains(name)) {
                    return request.getHeader(name);
                }
                return null;
            }
        };
        filterChain.doFilter(wrappedRequest, response);
    }
}
