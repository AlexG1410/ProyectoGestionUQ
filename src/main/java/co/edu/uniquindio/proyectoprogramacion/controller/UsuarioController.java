package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /api/usuarios/responsables-activos - Listar usuarios activos para asignar como responsables
     * Roles: ADMINISTRATIVO, COORDINADOR
     */
    @GetMapping("/responsables-activos")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<List<UsuarioSimpleDTO>>> listarResponsablesActivos() {
        List<UsuarioSimpleDTO> response = usuarioService.listarResponsablesActivos();
        return ResponseEntity.ok(ApiResponseDTO.<List<UsuarioSimpleDTO>>builder()
                .success(true)
                .message("Responsables activos obtenidos correctamente")
                .data(response)
                .build());
    }
}