package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.*;
import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponseDTO<AuthMeResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ApiResponseDTO.<AuthMeResponseDTO>builder()
                .success(true)
                .message("Usuario registrado correctamente")
                .timestamp(LocalDateTime.now())
                .data(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponseDTO<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ApiResponseDTO.<LoginResponseDTO>builder()
                .success(true)
                .message("Login exitoso")
                .timestamp(LocalDateTime.now())
                .data(authService.login(request))
                .build();
    }

    @GetMapping("/me")
    public ApiResponseDTO<AuthMeResponseDTO> me() {
        return ApiResponseDTO.<AuthMeResponseDTO>builder()
                .success(true)
                .message("Usuario autenticado obtenido correctamente")
                .timestamp(LocalDateTime.now())
                .data(authService.me())
                .build();
    }
}