package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.model.enums.AccionHistorial;

import java.util.UUID;

public interface HistorialService {
    void registrar(UUID solicitudId, UUID actorId, AccionHistorial accion, String detalle, String observaciones);
}
