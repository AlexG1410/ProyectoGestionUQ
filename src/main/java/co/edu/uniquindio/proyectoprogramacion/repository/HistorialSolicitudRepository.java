package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.HistorialSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistorialSolicitudRepository extends JpaRepository<HistorialSolicitud, UUID> {
    List<HistorialSolicitud> findBySolicitudIdOrderByFechaHoraAsc(UUID solicitudId);
}