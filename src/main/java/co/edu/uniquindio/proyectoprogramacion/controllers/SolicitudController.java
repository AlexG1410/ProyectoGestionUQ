package co.edu.uniquindio.proyectoprogramacion.controllers;

import co.edu.uniquindio.proyectoprogramacion.dto.*;
import co.edu.uniquindio.proyectoprogramacion.services.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<SolicitudResponseDTO> registrar(
            @Valid @RequestBody SolicitudCreateDTO dto,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.registrar(dto, actor(principal)));
    }

    @PutMapping("/{id}/clasificar-priorizar")
    public ResponseEntity<SolicitudResponseDTO> clasificarPriorizar(
            @PathVariable Long id,
            @Valid @RequestBody ClasificarPriorizarDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.clasificarPriorizar(id, dto, actor(principal)));
    }

    @PutMapping("/{id}/asignar")
    public ResponseEntity<SolicitudResponseDTO> asignarResponsable(
            @PathVariable Long id,
            @Valid @RequestBody AsignarResponsableDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.asignarResponsable(id, dto, actor(principal)));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, dto, actor(principal)));
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudResponseDTO> cerrar(
            @PathVariable Long id,
            @Valid @RequestBody CerrarSolicitudDTO dto,
            Principal principal) {
        return ResponseEntity.ok(solicitudService.cerrarSolicitud(id, dto, actor(principal)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<SolicitudResponseDTO>> consultar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Long responsableId) {
        return ResponseEntity.ok(solicitudService.consultar(estado, tipo, prioridad, responsableId));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialResponseDTO>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerHistorial(id));
    }

    @GetMapping("/{id}/resumen")
    public ResponseEntity<String> resumen(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.generarResumenIA(id));
    }
}