package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long>, JpaSpecificationExecutor<Solicitud> {

    Page<Solicitud> findBySolicitante(Usuario solicitante, Pageable pageable);
}