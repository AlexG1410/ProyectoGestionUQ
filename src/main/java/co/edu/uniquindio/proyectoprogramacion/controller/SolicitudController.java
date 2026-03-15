package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.*;
import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import co.edu.uniquindio.proyectoprogramacion.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping("/solicitudes")
    public ApiResponseDTO<SolicitudResponseDTO> crear(@Valid @RequestBody SolicitudCreateDTO request) {
        return response("Solicitud registrada correctamente", solicitudService.crearSolicitud(request));
    }

    @GetMapping("/solicitudes")
    public ApiResponseDTO<Page<SolicitudResponseDTO>> consultarSolicitudes(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) TipoSolicitud tipoSolicitud,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaHoraRegistro,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));

        return ApiResponseDTO.<Page<SolicitudResponseDTO>>builder()
                .success(true)
                .message("Consulta realizada correctamente")
                .timestamp(LocalDateTime.now())
                .data(solicitudService.consultarSolicitudes(estado, tipoSolicitud, prioridad, responsableId, pageable))
                .build();
    }

    @GetMapping("/solicitudes/{id}")
    public ApiResponseDTO<SolicitudResponseDTO> obtener(@PathVariable Long id) {
        return response("Solicitud encontrada", solicitudService.obtenerPorId(id));
    }

    @PutMapping("/solicitudes/{id}/clasificar")
    public ApiResponseDTO<SolicitudResponseDTO> clasificar(@PathVariable Long id,
                                                           @Valid @RequestBody ClasificarSolicitudRequestDTO request) {
        return response("Solicitud clasificada correctamente", solicitudService.clasificar(id, request));
    }

    @PutMapping("/solicitudes/{id}/priorizar")
    public ApiResponseDTO<SolicitudResponseDTO> priorizar(@PathVariable Long id,
                                                          @Valid @RequestBody PriorizarSolicitudRequestDTO request) {
        return response("Solicitud priorizada correctamente", solicitudService.priorizar(id, request));
    }

    @PutMapping("/solicitudes/{id}/asignar")
    public ApiResponseDTO<SolicitudResponseDTO> asignar(@PathVariable Long id,
                                                        @Valid @RequestBody AsignarResponsableRequestDTO request) {
        return response("Responsable asignado correctamente", solicitudService.asignar(id, request));
    }

    @PutMapping("/solicitudes/{id}/iniciar-atencion")
    public ApiResponseDTO<SolicitudResponseDTO> iniciarAtencion(@PathVariable Long id,
                                                                @RequestBody(required = false) ObservacionRequestDTO request) {
        return response("Solicitud pasada a EN_ATENCION correctamente", solicitudService.iniciarAtencion(id, request));
    }

    @PutMapping("/solicitudes/{id}/marcar-atendida")
    public ApiResponseDTO<SolicitudResponseDTO> marcarAtendida(@PathVariable Long id,
                                                               @RequestBody(required = false) ObservacionRequestDTO request) {
        return response("Solicitud marcada como atendida", solicitudService.marcarAtendida(id, request));
    }

    @PutMapping("/solicitudes/{id}/cerrar")
    public ApiResponseDTO<SolicitudResponseDTO> cerrar(@PathVariable Long id,
                                                       @Valid @RequestBody CerrarSolicitudRequestDTO request) {
        return response("Solicitud cerrada correctamente", solicitudService.cerrar(id, request));
    }

    @GetMapping("/solicitudes/{id}/historial")
    public ApiResponseDTO<List<HistorialResponseDTO>> historial(@PathVariable Long id) {
        return ApiResponseDTO.<List<HistorialResponseDTO>>builder()
                .success(true)
                .message("Historial obtenido correctamente")
                .timestamp(LocalDateTime.now())
                .data(solicitudService.historial(id))
                .build();
    }

    @PostMapping("/solicitudes/sugerir-clasificacion-prioridad")
    public ApiResponseDTO<SugerirClasificacionPrioridadResponseDTO> sugerirClasificacionPrioridad(
            @Valid @RequestBody SugerirClasificacionPrioridadRequestDTO request) {
        return ApiResponseDTO.<SugerirClasificacionPrioridadResponseDTO>builder()
                .success(true)
                .message("Sugerencia generada correctamente")
                .timestamp(LocalDateTime.now())
                .data(solicitudService.sugerirClasificacionPrioridad(request))
                .build();
    }

    @GetMapping("/usuarios/responsables-activos")
    public ApiResponseDTO<List<UsuarioSimpleDTO>> responsablesActivos() {
        return ApiResponseDTO.<List<UsuarioSimpleDTO>>builder()
                .success(true)
                .message("Responsables activos obtenidos correctamente")
                .timestamp(LocalDateTime.now())
                .data(solicitudService.responsablesActivos())
                .build();
    }

    @GetMapping("/mis-solicitudes")
    public ApiResponseDTO<Page<SolicitudResponseDTO>> misSolicitudes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaHoraRegistro,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));

        return ApiResponseDTO.<Page<SolicitudResponseDTO>>builder()
                .success(true)
                .message("Solicitudes obtenidas correctamente")
                .timestamp(LocalDateTime.now())
                .data(solicitudService.misSolicitudes(pageable))
                .build();
    }

    @GetMapping("/mis-solicitudes/{id}")
    public ApiResponseDTO<SolicitudResponseDTO> miSolicitud(@PathVariable Long id) {
        return response("Solicitud obtenida correctamente", solicitudService.miSolicitudPorId(id));
    }

    private ApiResponseDTO<SolicitudResponseDTO> response(String message, SolicitudResponseDTO data) {
        return ApiResponseDTO.<SolicitudResponseDTO>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }
}