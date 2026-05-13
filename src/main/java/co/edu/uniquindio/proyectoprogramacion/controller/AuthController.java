package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.*;
import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.service.AuthService;
import co.edu.uniquindio.proyectoprogramacion.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityUtils securityUtils;

    /**
     * POST /api/auth/login - Autenticar usuario y obtener JWT
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = authService.login(dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.<LoginResponseDTO>builder()
                        .success(true)
                        .message("Login exitoso")
                        .data(response)
                        .build());
    }

    /**
     * GET /api/auth/me - Obtener datos del usuario autenticado
     * Requiere JWT token en Authorization header
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDTO<AuthMeResponseDTO>> me() {
        UUID userId = securityUtils.getUsuarioId();
        AuthMeResponseDTO response = authService.me(userId);
        return ResponseEntity.ok(ApiResponseDTO.<AuthMeResponseDTO>builder()
                .success(true)
                .message("Datos del usuario obtenidos correctamente")
                .data(response)
                .build());
    }

    /**
     * POST /api/auth/refresh - Refrescar token JWT
     * Endpoint público - no requiere autenticación previa
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponseDTO<RefreshTokenResponseDTO>> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO dto) {
        RefreshTokenResponseDTO response = authService.refresh(dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.<RefreshTokenResponseDTO>builder()
                        .success(true)
                        .message("Token refrescado exitosamente")
                        .data(response)
                        .build());
    }
}
