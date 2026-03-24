package com.example.habittracker.Auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenUtil tokenUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, TokenUtil tokenUtil) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenUtil = tokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Bỏ qua filter cho các đường dẫn /login và /register
        String requestPath = request.getServletPath();
        if (requestPath.equals("/login") || requestPath.equals("/register")|| requestPath.equals("/css/auth_login.css")|| requestPath.equals("/css/auth_register.css") || requestPath.equals("/login_with_google")|| requestPath.equals("/forgot-password")|| requestPath.equals("/reset-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = tokenUtil.getTokenFromCookies(request);
        String email = null;

        // Nếu không có token, chuyển hướng về /login
        if (token == null) {
            response.sendRedirect("/login");
            return;
        }

        // Kiểm tra token
        try {
            email = jwtUtil.getEmailFromToken(token);
        } catch (RuntimeException e) {
            // Token hết hạn hoặc không hợp lệ, chuyển hướng về /login
            response.sendRedirect("/login");
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)) { // Không phải xác thực ẩn danh
            filterChain.doFilter(request, response);
            return;
        }

        // Nếu token hợp lệ và chưa có Authentication trong SecurityContext
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtUtil.validateToken(token)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
