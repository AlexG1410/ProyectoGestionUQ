package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, UUID>, JpaSpecificationExecutor<Solicitud> {

    Page<Solicitud> findBySolicitante(Usuario solicitante, Pageable pageable);
    
    Page<Solicitud> findBySolicitanteId(UUID solicitanteId, Pageable pageable);
}