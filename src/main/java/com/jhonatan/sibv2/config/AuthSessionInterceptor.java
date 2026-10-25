package com.jhonatan.sibv2.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthSessionInterceptor implements HandlerInterceptor {
    public static final String AUTHENTICATED_EMPLOYEE = "authenticatedEmployee";

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String LOGOUT_PATH = "/api/auth/logout";
    private static final String EMPLOYEE_PATH = "/api/funcionarios";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (isPublicRequest(request) || isLoggedIn(request) || isEmployeeSignup(request)) {
            return true;
        }

        sendUnauthorized(response);
        return false;
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        return HttpMethod.OPTIONS.matches(method)
                || HttpMethod.GET.matches(method)
                || HttpMethod.HEAD.matches(method)
                || LOGIN_PATH.equals(path)
                || LOGOUT_PATH.equals(path);
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(AUTHENTICATED_EMPLOYEE) != null;
    }

    private boolean isEmployeeSignup(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
                && EMPLOYEE_PATH.equals(request.getRequestURI());
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":401,\"detail\":\"Faça login para realizar esta operação.\"}");
    }
}
