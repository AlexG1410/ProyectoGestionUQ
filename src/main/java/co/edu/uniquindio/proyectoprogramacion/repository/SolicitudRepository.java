package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, UUID>, JpaSpecificationExecutor<Solicitud> {

    Page<Solicitud> findBySolicitante(Usuario solicitante, Pageable pageable);
    
    Page<Solicitud> findBySolicitanteId(UUID solicitanteId, Pageable pageable);

    @EntityGraph(attributePaths = {"solicitante", "responsable"})

    @Query("select s from Solicitud s where s.id = :id")
    Optional<Solicitud> findByIdWithUsuarios(@Param("id") UUID id);
}