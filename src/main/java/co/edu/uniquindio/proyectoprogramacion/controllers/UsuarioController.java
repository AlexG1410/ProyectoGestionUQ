package co.edu.uniquindio.proyectoprogramacion.controllers;

import co.edu.uniquindio.proyectoprogramacion.dto.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponse;
import co.edu.uniquindio.proyectoprogramacion.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios", description = "Consulta de usuarios y responsables activos")
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;


    @Operation(summary = "Listar responsables activos (administrativos y coordinadores)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Responsables activos obtenidos correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @GetMapping("/responsables-activos")
    public ResponseEntity<ApiResponse<List<UsuarioSimpleDTO>>> listarResponsablesActivos() {
        List<UsuarioSimpleDTO> responsables = usuarioService.listarResponsablesActivos();
        return ResponseEntity.ok(ApiResponse.ok("Responsables activos obtenidos correctamente", responsables));
    }
}