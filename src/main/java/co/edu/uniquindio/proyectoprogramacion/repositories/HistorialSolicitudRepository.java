package co.edu.uniquindio.proyectoprogramacion.repositories;

import co.edu.uniquindio.proyectoprogramacion.model.HistorialSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialSolicitudRepository extends JpaRepository<HistorialSolicitud, Long> {
    List<HistorialSolicitud> findBySolicitud_IdOrderByFechaHoraAsc(Long solicitudId);
}