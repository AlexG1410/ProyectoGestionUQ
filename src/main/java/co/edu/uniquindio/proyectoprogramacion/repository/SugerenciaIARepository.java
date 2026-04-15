package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.SugerenciaIA;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SugerenciaIARepository extends JpaRepository<SugerenciaIA, UUID> {
    List<SugerenciaIA> findBySolicitudId(UUID solicitudId);
}
