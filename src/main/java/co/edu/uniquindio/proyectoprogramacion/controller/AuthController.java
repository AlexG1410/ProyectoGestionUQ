package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.*;
import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.security.CustomUserDetails;
import co.edu.uniquindio.proyectoprogramacion.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        UUID userId = userDetails.getId();
        
        AuthMeResponseDTO response = authService.me(userId);
        return ResponseEntity.ok(ApiResponseDTO.<AuthMeResponseDTO>builder()
                .success(true)
                .message("Datos del usuario obtenidos correctamente")
                .data(response)
                .build());
    }
}