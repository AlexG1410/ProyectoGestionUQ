package co.edu.uniquindio.proyectoprogramacion.services;

import co.edu.uniquindio.proyectoprogramacion.dto.*;

import java.util.List;

public interface SolicitudService {
    SolicitudResponseDTO registrar(SolicitudCreateDTO dto, String usuarioActor);
    SolicitudResponseDTO clasificarPriorizar(Long solicitudId, ClasificarPriorizarDTO dto, String usuarioActor);
    SolicitudResponseDTO asignarResponsable(Long solicitudId, AsignarResponsableDTO dto, String usuarioActor);
    SolicitudResponseDTO cambiarEstado(Long solicitudId, CambiarEstadoDTO dto, String usuarioActor);
    SolicitudResponseDTO cerrarSolicitud(Long solicitudId, CerrarSolicitudDTO dto, String usuarioActor);

    SolicitudResponseDTO obtenerPorId(Long id);
    List<SolicitudResponseDTO> consultar(String estado, String tipo, String prioridad, Long responsableId);
    List<HistorialResponseDTO> obtenerHistorial(Long solicitudId);

    // IA opcional (RF-09/RF-10) - puede devolver mock inicialmente
    String generarResumenIA(Long solicitudId);
}