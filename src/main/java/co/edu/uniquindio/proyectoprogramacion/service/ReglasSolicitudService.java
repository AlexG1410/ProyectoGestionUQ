package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.Prioridad;

public interface ReglasSolicitudService {
    Prioridad calcularPrioridad(Solicitud solicitud);
    String construirJustificacionPrioridad(Solicitud solicitud);
    SugerirClasificacionPrioridadResponseDTO sugerirClasificacionPrioridad(SugerirClasificacionPrioridadRequestDTO request);
}