package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.TransicionEstado;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TransicionEstadoRepository extends JpaRepository<TransicionEstado, UUID> {
    Optional<TransicionEstado> findByDesdeAndHaciaAndPermitidaTrue(EstadoSolicitud desde, EstadoSolicitud hacia);
}
