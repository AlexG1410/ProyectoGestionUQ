package co.edu.uniquindio.proyectoprogramacion.controllers;

import co.edu.uniquindio.proyectoprogramacion.dto.*;
import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponse;
import co.edu.uniquindio.proyectoprogramacion.services.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import co.edu.uniquindio.proyectoprogramacion.dto.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.services.rules.PrioridadRuleEngine;
import java.security.Principal;
import java.util.List;

@Tag(name = "Solicitudes", description = "Gestión de solicitudes académicas: registro, clasificación, asignación, estados, cierre e historial")
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final PrioridadRuleEngine prioridadRuleEngine;

    private String actor(Principal principal) {
        return principal != null ? principal.getName() : "system";
    }

    // RF-01: Registrar solicitud (estudiante/admin/coord)
    @Operation(summary = "Registrar una nueva solicitud académica")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Solicitud registrada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ESTUDIANTE','ADMINISTRATIVO','COORDINADOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<SolicitudResponseDTO>> registrar(
            @Valid @RequestBody SolicitudCreateDTO dto,
            Principal principal) {
        SolicitudResponseDTO response = solicitudService.registrar(dto, actor(principal));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Solicitud registrada correctamente", response));
    }

    @Operation(summary = "Sugerir prioridad mediante motor de reglas")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prioridad sugerida correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @PostMapping("/sugerir-prioridad")
    public ResponseEntity<ApiResponse<SugerirPrioridadResponseDTO>> sugerirPrioridad(
            @Valid @RequestBody SugerirPrioridadRequestDTO dto) {

        SugerirPrioridadResponseDTO sugerencia = prioridadRuleEngine.sugerir(dto);

        return ResponseEntity.ok(
                ApiResponse.ok("Prioridad sugerida correctamente", sugerencia)
        );
    }

    // RF-02 y RF-03: Clasificar + priorizar (admin/coord)
    @Operation(summary = "Clasificar y priorizar una solicitud")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud clasificada/priorizada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Regla de negocio inválida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @PutMapping("/{id}/clasificar-priorizar")
    public ResponseEntity<SolicitudResponseDTO> clasificarPriorizar(
            @PathVariable Long id,
            @Valid @RequestBody ClasificarPriorizarDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.clasificarPriorizar(id, dto, actor(principal)));
    }

    // RF-05: Asignar responsable (coord/admin)
    @Operation(summary = "Asignar responsable a una solicitud")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Responsable asignado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Responsable inválido o inactivo"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud o responsable no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @PutMapping("/{id}/asignar")
    public ResponseEntity<SolicitudResponseDTO> asignarResponsable(
            @PathVariable Long id,
            @Valid @RequestBody AsignarResponsableDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.asignarResponsable(id, dto, actor(principal)));
    }

    // RF-04: Gestión de estados (admin/coord)
    @Operation(summary = "Cambiar estado de una solicitud")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, dto, actor(principal)));
    }

    // RF-08: Cierre (admin/coord)
    @Operation(summary = "Cerrar una solicitud (solo si está ATENDIDA)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud cerrada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "La solicitud no puede cerrarse en su estado actual"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @PutMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudResponseDTO> cerrar(
            @PathVariable Long id,
            @Valid @RequestBody CerrarSolicitudDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.cerrarSolicitud(id, dto, actor(principal)));
    }

    // Consultas (todos autenticados)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    @Operation(summary = "Consultar solicitudes con filtros opcionales")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta realizada correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SolicitudResponseDTO>>> consultar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Long responsableId) {
        List<SolicitudResponseDTO> lista = solicitudService.consultar(estado, tipo, prioridad, responsableId);
        return ResponseEntity.ok(ApiResponse.ok("Consulta realizada correctamente", lista));
    }

    // RF-06: Historial auditable
    @Operation(summary = "Obtener historial auditable de una solicitud")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<HistorialResponseDTO>>> historial(@PathVariable Long id) {
        List<HistorialResponseDTO> historial = solicitudService.obtenerHistorial(id);
        return ResponseEntity.ok(ApiResponse.ok("Historial obtenido correctamente", historial));
    }

    // RF-09/RF-10 opcional (fallback sin IA externa)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/resumen")
    public ResponseEntity<ApiResponse<String>> resumen(@PathVariable Long id) {
        String resumen = solicitudService.generarResumenIA(id);
        return ResponseEntity.ok(ApiResponse.ok("Resumen generado correctamente", resumen));
    }
}