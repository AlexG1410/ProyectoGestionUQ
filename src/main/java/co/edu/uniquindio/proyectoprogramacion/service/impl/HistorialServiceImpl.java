package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.model.entity.HistorialSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.AccionHistorial;
import co.edu.uniquindio.proyectoprogramacion.repository.HistorialSolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistorialServiceImpl implements HistorialService {

    private final HistorialSolicitudRepository historialRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void registrar(UUID solicitudId, UUID actorId, AccionHistorial accion, String detalle, String observaciones) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        Usuario actor = usuarioRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario actor no encontrado"));

        HistorialSolicitud historial = HistorialSolicitud.builder()
                .solicitud(solicitud)
                .actor(actor)
                .fechaHora(LocalDateTime.now())
                .accion(accion)
                .detalle(detalle)
                .observaciones(observaciones)
                .build();

        historialRepository.save(historial);
    }
}