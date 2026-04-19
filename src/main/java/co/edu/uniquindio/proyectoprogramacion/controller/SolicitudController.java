package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.*;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.security.CustomUserDetails;
import co.edu.uniquindio.proyectoprogramacion.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

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
     * POST /api/solicitudes - Registrar nueva solicitud
     * Roles: ESTUDIANTE, ADMINISTRATIVO, COORDINADOR
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SolicitudResponseDTO>> registrar(
            @Valid @RequestBody SolicitudCreateDTO dto) {
        UUID usuarioId = getUsuarioId();
        SolicitudResponseDTO response = solicitudService.registrar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<SolicitudResponseDTO>builder()
                        .success(true)
                        .message("Solicitud registrada correctamente")
                        .data(response)
                        .build());
    }

    /**
     * GET /api/solicitudes - Consultar solicitudes con filtros
     * Roles: ADMINISTRATIVO, COORDINADOR, CONSULTOR
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR', 'CONSULTOR')")
    public ResponseEntity<ApiResponseDTO<List<SolicitudResponseDTO>>> consultar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) UUID responsableId) {
        
        // Convertir strings a enums de manera segura
        EstadoSolicitud estadoEnum = null;
        TipoSolicitud tipoEnum = null;
        Prioridad prioridadEnum = null;
        
        if (estado != null && !estado.isEmpty()) {
            try {
                estadoEnum = EstadoSolicitud.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si el valor no es válido, ignorar y buscar sin este filtro
            }
        }
        
        if (tipo != null && !tipo.isEmpty()) {
            try {
                tipoEnum = TipoSolicitud.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si el valor no es válido, ignorar y buscar sin este filtro
            }
        }
        
        if (prioridad != null && !prioridad.isEmpty()) {
            try {
                prioridadEnum = Prioridad.valueOf(prioridad.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si el valor no es válido, ignorar y buscar sin este filtro
            }
        }
        
        FiltroSolicitudesDTO filtro = FiltroSolicitudesDTO.builder()
                .estado(estadoEnum)
                .tipo(tipoEnum)
                .prioridad(prioridadEnum)
                .responsableId(responsableId)
                .build();
        
        List<SolicitudResponseDTO> response = solicitudService.consultar(filtro);
        return ResponseEntity.ok(ApiResponseDTO.<List<SolicitudResponseDTO>>builder()
                .success(true)
                .message("Solicitudes consultadas correctamente")
                .data(response)
                .build());
    }

    /**
     * GET /api/solicitudes/mis-solicitudes - Consultar mis solicitudes (usuario autenticado)
     * Roles: ESTUDIANTE, ADMINISTRATIVO, COORDINADOR
     */
    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<List<SolicitudResponseDTO>>> misSolicitudes(
            @RequestParam(required = false) String estado) {
        UUID usuarioId = getUsuarioId();
        
        EstadoSolicitud estadoEnum = null;
        if (estado != null && !estado.isEmpty()) {
            try {
                estadoEnum = EstadoSolicitud.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si el valor no es válido, ignorar
            }
        }
        
        FiltroSolicitudesDTO filtro = FiltroSolicitudesDTO.builder()
                .estado(estadoEnum)
                .build();
        
        List<SolicitudResponseDTO> response = solicitudService.obtenerMisSolicitudes(usuarioId, filtro);
        return ResponseEntity.ok(ApiResponseDTO.<List<SolicitudResponseDTO>>builder()
                .success(true)
                .message("Mis solicitudes obtenidas correctamente")
                .data(response)
                .build());
    }

    /**
     * GET /api/solicitudes/mis-solicitudes/{id} - Obtener detalle de mi solicitud
     * Roles: ESTUDIANTE, ADMINISTRATIVO, COORDINADOR
     */
    @GetMapping("/mis-solicitudes/{id}")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SolicitudResponseDTO>> miSolicitud(@PathVariable UUID id) {
        UUID usuarioId = getUsuarioId();
        SolicitudResponseDTO response = solicitudService.obtenerMiSolicitud(id, usuarioId);
        return ResponseEntity.ok(ApiResponseDTO.<SolicitudResponseDTO>builder()
                .success(true)
                .message("Solicitud obtenida correctamente")
                .data(response)
                .build());
    }

    /**
     * GET /api/solicitudes/{id} - Obtener solicitud por ID
     * Roles: ESTUDIANTE, ADMINISTRATIVO, COORDINADOR, CONSULTOR
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR', 'CONSULTOR')")
    public ResponseEntity<ApiResponseDTO<SolicitudResponseDTO>> obtenerPorId(@PathVariable UUID id) {
        UUID usuarioId = getUsuarioId();
        RolUsuario rol = getRolUsuario();
        SolicitudResponseDTO response = solicitudService.obtenerPorId(id, usuarioId, rol);
        return ResponseEntity.ok(ApiResponseDTO.<SolicitudResponseDTO>builder()
                .success(true)
                .message("Solicitud obtenida correctamente")
                .data(response)
                .build());
    }

    /**
     * PUT /api/solicitudes/{id}/clasificar - Clasificar y priorizar solicitud
     * Roles: ADMINISTRATIVO, COORDINADOR
     */
    @PutMapping("/{id}/clasificar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SolicitudResponseDTO>> clasificarPriorizar(
            @PathVariable UUID id,
            @Valid @RequestBody ClasificarPriorizarDTO dto) {
        UUID usuarioId = getUsuarioId();
        SolicitudResponseDTO response = solicitudService.clasificarPriorizar(id, dto, usuarioId);
        return ResponseEntity.ok(ApiResponseDTO.<SolicitudResponseDTO>builder()
                .success(true)
                .message("Solicitud clasificada y priorizada correctamente")
                .data(response)
                .build());
    }

    /**
     * PUT /api/solicitudes/{id}/asignar - Asignar responsable a solicitud
     * Roles: ADMINISTRATIVO, COORDINADOR
     */
    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SolicitudResponseDTO>> asignarResponsable(
            @PathVariable UUID id,
            @Valid @RequestBody AsignarResponsableDTO dto) {
        UUID usuarioId = getUsuarioId();
        SolicitudResponseDTO response = solicitudService.asignarResponsable(id, dto, usuarioId);
        return ResponseEntity.ok(ApiResponseDTO.<SolicitudResponseDTO>builder()
                .success(true)
                .message("Responsable asignado correctamente")
                .data(response)
                .build());
    }

    /**
     * PUT /api/solicitudes/{id}/iniciar-atencion - Iniciar atención de solicitud
     * Roles: ADMINISTRATIVO, COORDINADOR
     */
    @PutMapping("/{id}/iniciar-atencion")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SolicitudResponseDTO>> iniciarAtencion(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "") String observacion) {
        UUID usuarioId = getUsuarioId();
        SolicitudResponseDTO response = solicitudService.iniciarAtencion(id, observacion, usuarioId);
        return ResponseEntity.ok(ApiResponseDTO.<SolicitudResponseDTO>builder()
                .success(true)
                .message("Atención iniciada correctamente")
                .data(response)
                .build());
    }

    /**
     * PUT /api/solicitudes/{id}/marcar-atendida - Marcar solicitud como atendida
     * Roles: ADMINISTRATIVO, COORDINADOR
     */
    @PutMapping("/{id}/marcar-atendida")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SolicitudResponseDTO>> marcarAtendida(
            @PathVariable UUID id,
            @Valid @RequestBody MarcarAtendidoDTO dto) {
        UUID usuarioId = getUsuarioId();
        SolicitudResponseDTO response = solicitudService.marcarAtendida(id, dto, usuarioId);
        return ResponseEntity.ok(ApiResponseDTO.<SolicitudResponseDTO>builder()
                .success(true)
                .message("Solicitud marcada como atendida correctamente")
                .data(response)
                .build());
    }

    /**
     * PUT /api/solicitudes/{id}/cerrar - Cerrar solicitud
     * Roles: ADMINISTRATIVO, COORDINADOR
     */
    @PutMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'COORDINADOR')")
    public ResponseEntity<ApiResponseDTO<SolicitudResponseDTO>> cerrar(
            @PathVariable UUID id,
            @Valid @RequestBody CerrarSolicitudDTO dto) {
        UUID usuarioId = getUsuarioId();
        SolicitudResponseDTO response = solicitudService.cerrar(id, dto, usuarioId);
        return ResponseEntity.ok(ApiResponseDTO.<SolicitudResponseDTO>builder()
                .success(true)
                .message("Solicitud cerrada correctamente")
                .data(response)
                .build());
    }

    /**
     * GET /api/solicitudes/{id}/historial - Obtener historial de solicitud
     * Roles: ESTUDIANTE, ADMINISTRATIVO, COORDINADOR, CONSULTOR
     */
    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRATIVO', 'COORDINADOR', 'CONSULTOR')")
    public ResponseEntity<ApiResponseDTO<List<HistorialResponseDTO>>> historial(@PathVariable UUID id) {
        UUID usuarioId = getUsuarioId();
        RolUsuario rol = getRolUsuario();
        List<HistorialResponseDTO> response = solicitudService.historial(id, usuarioId, rol);
        return ResponseEntity.ok(ApiResponseDTO.<List<HistorialResponseDTO>>builder()
                .success(true)
                .message("Historial obtenido correctamente")
                .data(response)
                .build());
    }
}