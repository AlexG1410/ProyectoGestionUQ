package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.service.IAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class IAController {

    private final IAService iaService;

    /**
     * GET /api/solicitudes/{id}/resumen - Generar resumen automático de solicitud (IA)
     * Roles: ESTUDIANTE, ADMINISTRATIVO, COORDINADOR, CONSULTOR
     */
    @GetMapping("/{id}/resumen")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR', 'CONSULTOR')")
    public ResponseEntity<ApiResponseDTO<String>> resumir(@PathVariable UUID id) {
        String response = iaService.resumirSolicitud(id);
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
