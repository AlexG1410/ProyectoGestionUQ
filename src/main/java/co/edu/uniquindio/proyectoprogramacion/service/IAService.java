package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;

import java.util.UUID;

public interface IAService {
    String resumirSolicitud(UUID solicitudId, UUID usuarioId, RolUsuario rol);
    SugerirPrioridadResponseDTO sugerirPrioridad(SugerirPrioridadRequestDTO dto);
    SugerirClasificacionPrioridadResponseDTO sugerirClasificacionYPrioridad(SugerirClasificacionPrioridadRequestDTO dto);
}
