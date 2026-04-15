package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.ReglaPriorizacion;
import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReglaPriorizacionRepository extends JpaRepository<ReglaPriorizacion, UUID> {
    List<ReglaPriorizacion> findByActivaTrueAndTipoSolicitudAndImpactoAcademico(TipoSolicitud tipoSolicitud, ImpactoAcademico impactoAcademico);
}
