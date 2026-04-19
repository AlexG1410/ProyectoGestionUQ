package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.*;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;

import java.util.*;

public interface SolicitudService {
    SolicitudResponseDTO registrar(SolicitudCreateDTO dto, UUID actorId);
    List<SolicitudResponseDTO> consultar(FiltroSolicitudesDTO filtro);
    SolicitudResponseDTO obtenerPorId(UUID solicitudId, UUID usuarioId, RolUsuario rol);
    List<SolicitudResponseDTO> obtenerMisSolicitudes(UUID solicitanteId, FiltroSolicitudesDTO filtro);
    SolicitudResponseDTO obtenerMiSolicitud(UUID solicitudId, UUID solicitanteId);
    SolicitudResponseDTO clasificarPriorizar(UUID solicitudId, ClasificarPriorizarDTO dto, UUID actorId);
    SolicitudResponseDTO asignarResponsable(UUID solicitudId, AsignarResponsableDTO dto, UUID actorId);
    SolicitudResponseDTO iniciarAtencion(UUID solicitudId, String observacion, UUID actorId);
    SolicitudResponseDTO marcarAtendida(UUID solicitudId, MarcarAtendidoDTO dto, UUID actorId);
    SolicitudResponseDTO cerrar(UUID solicitudId, CerrarSolicitudDTO dto, UUID actorId);
    List<HistorialResponseDTO> historial(UUID solicitudId, UUID usuarioId, RolUsuario rol);
}