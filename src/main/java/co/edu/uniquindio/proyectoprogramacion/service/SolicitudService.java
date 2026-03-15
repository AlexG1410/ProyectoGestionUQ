package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.*;
import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SolicitudService {
    SolicitudResponseDTO crearSolicitud(SolicitudCreateDTO request);
    SolicitudResponseDTO clasificar(Long id, ClasificarSolicitudRequestDTO request);
    SolicitudResponseDTO priorizar(Long id, PriorizarSolicitudRequestDTO request);
    SolicitudResponseDTO asignar(Long id, AsignarResponsableRequestDTO request);
    SolicitudResponseDTO iniciarAtencion(Long id, ObservacionRequestDTO request);
    SolicitudResponseDTO marcarAtendida(Long id, ObservacionRequestDTO request);
    SolicitudResponseDTO cerrar(Long id, CerrarSolicitudRequestDTO request);
    SolicitudResponseDTO obtenerPorId(Long id);

    Page<SolicitudResponseDTO> consultarSolicitudes(
            EstadoSolicitud estado,
            TipoSolicitud tipoSolicitud,
            Prioridad prioridad,
            Long responsableId,
            Pageable pageable
    );

    Page<SolicitudResponseDTO> misSolicitudes(Pageable pageable);
    SolicitudResponseDTO miSolicitudPorId(Long id);
    List<HistorialResponseDTO> historial(Long id);
    List<UsuarioSimpleDTO> responsablesActivos();
    SugerirClasificacionPrioridadResponseDTO sugerirClasificacionPrioridad(SugerirClasificacionPrioridadRequestDTO request);
}