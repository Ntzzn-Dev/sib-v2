package com.jhonatan.sibv2.controllers;

import com.jhonatan.sibv2.dto.requests.LoginRequest;
import com.jhonatan.sibv2.dto.responses.LoginResponse;
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

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }

}
