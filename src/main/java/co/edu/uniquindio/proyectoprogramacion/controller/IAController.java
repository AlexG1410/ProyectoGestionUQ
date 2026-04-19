package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.security.CustomUserDetails;
import co.edu.uniquindio.proyectoprogramacion.service.IAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class IAController {

    private final IAService iaService;

    /**
     * Obtiene el ID del usuario autenticado desde el JWT
     */
    private UUID getUsuarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        }
        throw new IllegalStateException("Usuario no autenticado");
    }

    /**
     * Obtiene el rol del usuario autenticado desde el JWT
     */
    private RolUsuario getRolUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            String authority = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse(null);
            if (authority != null) {
                try {
                    return RolUsuario.valueOf(authority);
                } catch (IllegalArgumentException e) {
                    // Continuar
                }
            }
        }
        throw new IllegalStateException("Rol de usuario no determinable");
    }

    /**
     * GET /api/solicitudes/{id}/resumen - Generar resumen automático de solicitud (IA)
     * Roles: ESTUDIANTE, ADMINISTRATIVO, COORDINADOR, CONSULTOR
     */
    @GetMapping("/{id}/resumen")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR', 'CONSULTOR')")
    public ResponseEntity<ApiResponseDTO<String>> resumir(@PathVariable UUID id) {
        UUID usuarioId = getUsuarioId();
        RolUsuario rol = getRolUsuario();
        String response = iaService.resumirSolicitud(id, usuarioId, rol);
        return ResponseEntity.ok(ApiResponseDTO.<String>builder()
                .success(true)
                .message("Resumen generado correctamente")
                .data(response)
                .build());
    }

    /**
     * POST /api/solicitudes/sugerir-prioridad - Sugerir prioridad para una solicitud
     * Roles: ADMINISTRATIVO, COORDINADOR
     */
    @PostMapping("/sugerir-prioridad")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SugerirPrioridadResponseDTO>> sugerirPrioridad(
            @Valid @RequestBody SugerirPrioridadRequestDTO dto) {
        SugerirPrioridadResponseDTO response = iaService.sugerirPrioridad(dto);
        return ResponseEntity.ok(ApiResponseDTO.<SugerirPrioridadResponseDTO>builder()
                .success(true)
                .message("Prioridad sugerida correctamente")
                .data(response)
                .build());
    }

    /**
     * POST /api/solicitudes/sugerir-clasificacion-prioridad - Sugerir clasificación y prioridad
     * Roles: ADMINISTRATIVO, COORDINADOR
     */
    @PostMapping("/sugerir-clasificacion-prioridad")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SugerirClasificacionPrioridadResponseDTO>> sugerirClasificacion(
            @Valid @RequestBody SugerirClasificacionPrioridadRequestDTO dto) {
        SugerirClasificacionPrioridadResponseDTO response = iaService.sugerirClasificacionYPrioridad(dto);
        return ResponseEntity.ok(ApiResponseDTO.<SugerirClasificacionPrioridadResponseDTO>builder()
                .success(true)
                .message("Sugerencia generada correctamente")
                .data(response)
                .build());
    }
}
