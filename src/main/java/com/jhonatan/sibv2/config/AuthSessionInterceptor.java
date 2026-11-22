package com.jhonatan.sibv2.config;

import com.jhonatan.sibv2.entities.Funcionario;
import com.jhonatan.sibv2.repositories.FuncionarioRepository;
import com.jhonatan.sibv2.utils.FormatadorDocumento;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthSessionInterceptor implements HandlerInterceptor {
    public static final String AUTHENTICATED_EMPLOYEE = "authenticatedEmployee";

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String LOGOUT_PATH = "/api/auth/logout";
    private static final String PROFILE_PATH = "/api/auth/me";
    private static final String FORGOT_PASSWORD_PATH = "/api/auth/esqueci-senha";
    private static final String EMPLOYEE_PATH = "/api/funcionarios";
    private static final String ADDRESS_PATH = "/api/enderecos";
    private final FuncionarioRepository funcionarioRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (isReadOrAuthRequest(request) || isBootstrapSignup(request)) {
            return true;
        }
        if (PROFILE_PATH.equals(request.getRequestURI())
                && (HttpMethod.GET.matches(request.getMethod()) || HttpMethod.PUT.matches(request.getMethod()))
                && isLoggedIn(request)) {
            return true;
        }
        if (isLoggedIn(request) && isAllowedAttendantMutation(request)) {
            return true;
        }
        if (isAdministrator(request)) {
            return true;
        }

        if (isLoggedIn(request)) {
            sendForbidden(response);
            return false;
        }

        sendUnauthorized(response);
        return false;
    }

    private boolean isReadOrAuthRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        return HttpMethod.OPTIONS.matches(method)
                || HttpMethod.GET.matches(method) && !PROFILE_PATH.equals(path)
                || HttpMethod.HEAD.matches(method) && !PROFILE_PATH.equals(path)
                || LOGIN_PATH.equals(path)
                || FORGOT_PASSWORD_PATH.equals(path)
                || LOGOUT_PATH.equals(path);
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object cpf = session.getAttribute(AUTHENTICATED_EMPLOYEE);
        if (!(cpf instanceof String cpfValue)) return false;
        return funcionarioRepository.existsById(FormatadorDocumento.normalizarCpf(cpfValue));
    }

    private boolean isBootstrapSignup(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
                && (EMPLOYEE_PATH.equals(request.getRequestURI())
                || ADDRESS_PATH.equals(request.getRequestURI()))
                && funcionarioRepository.count() == 0;
    }

    private boolean isAllowedAttendantMutation(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (path.equals("/api/vendas") || path.startsWith("/api/vendas/")) return true;
        return HttpMethod.POST.matches(method)
                && (path.equals("/api/clientes") || path.equals(ADDRESS_PATH));
    }

    private boolean isAdministrator(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object cpf = session.getAttribute(AUTHENTICATED_EMPLOYEE);
        if (!(cpf instanceof String cpfValue)) return false;
        String normalizedCpf = FormatadorDocumento.normalizarCpf(cpfValue);
        return funcionarioRepository.findById(normalizedCpf)
                .map(Funcionario::getPerfil)
                .filter(perfil -> perfil == Funcionario.PERFIL_ADMIN)
                .isPresent();
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":401,\"detail\":\"Faça login para realizar esta operação.\"}");
    }

    private void sendForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":403,\"detail\":\"Esta operação é permitida somente para administradores.\"}");
    }
}
