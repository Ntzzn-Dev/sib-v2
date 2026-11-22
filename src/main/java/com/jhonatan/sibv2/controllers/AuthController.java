package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.LoginRequest;
import com.jhonatan.sibv2.dto.responses.LoginResponse;
import com.jhonatan.sibv2.dto.requests.ProfileRequest;
import com.jhonatan.sibv2.dto.responses.ProfileResponse;
import com.jhonatan.sibv2.services.AuthService;
import com.jhonatan.sibv2.config.AuthSessionInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = auth.login(request);
        session.setAttribute(AuthSessionInterceptor.AUTHENTICATED_EMPLOYEE, response.getCpfPessoa());
        return response;
    }

    @GetMapping("/me")
    public ProfileResponse myProfile(HttpSession session) {
        return auth.meuPerfil(cpfDaSessao(session));
    }

    @PutMapping("/me")
    public ProfileResponse updateMyProfile(@RequestBody ProfileRequest request, HttpSession session) {
        return auth.atualizarMeuPerfil(cpfDaSessao(session), request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }

    private String cpfDaSessao(HttpSession session) {
        Object cpf = session.getAttribute(AuthSessionInterceptor.AUTHENTICATED_EMPLOYEE);
        if (!(cpf instanceof String valor)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Faça login para realizar esta operação.");
        }
        return valor;
    }

}
