package co.edu.uniquindio.proyectoprogramacion.controllers;

import co.edu.uniquindio.proyectoprogramacion.dto.*;
import co.edu.uniquindio.proyectoprogramacion.services.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    private String actor(Principal principal) {
        return principal != null ? principal.getName() : "system";
    }

    // RF-01: Registrar solicitud (estudiante/admin/coord)
    @PreAuthorize("hasAnyRole('ESTUDIANTE','ADMINISTRATIVO','COORDINADOR')")
    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> registrar(
            @Valid @RequestBody SolicitudCreateDTO dto,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.registrar(dto, actor(principal)));
    }

    // RF-02 y RF-03: Clasificar + priorizar (admin/coord)
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @PutMapping("/{id}/clasificar-priorizar")
    public ResponseEntity<SolicitudResponseDTO> clasificarPriorizar(
            @PathVariable Long id,
            @Valid @RequestBody ClasificarPriorizarDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.clasificarPriorizar(id, dto, actor(principal)));
    }

    // RF-05: Asignar responsable (coord/admin)
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @PutMapping("/{id}/asignar")
    public ResponseEntity<SolicitudResponseDTO> asignarResponsable(
            @PathVariable Long id,
            @Valid @RequestBody AsignarResponsableDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.asignarResponsable(id, dto, actor(principal)));
    }

    // RF-04: Gestión de estados (admin/coord)
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, dto, actor(principal)));
    }

    // RF-08: Cierre (admin/coord)
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<SolicitudResponseDTO>> consultar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Long responsableId) {
        return ResponseEntity.ok(solicitudService.consultar(estado, tipo, prioridad, responsableId));
    }

    // RF-06: Historial auditable
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialResponseDTO>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerHistorial(id));
    }

    // RF-09/RF-10 opcional (fallback sin IA externa)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/resumen")
    public ResponseEntity<String> resumen(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.generarResumenIA(id));
    }
}