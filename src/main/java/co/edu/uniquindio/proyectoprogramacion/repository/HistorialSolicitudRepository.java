package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.HistorialSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialSolicitudRepository extends JpaRepository<HistorialSolicitud, Long> {

    List<HistorialSolicitud> findBySolicitudOrderByFechaHoraAsc(Solicitud solicitud);
}