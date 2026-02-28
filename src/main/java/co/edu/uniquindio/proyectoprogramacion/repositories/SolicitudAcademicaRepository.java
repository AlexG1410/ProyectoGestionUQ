package co.edu.uniquindio.proyectoprogramacion.repositories;

import co.edu.uniquindio.proyectoprogramacion.model.*;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.PrioridadSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SolicitudAcademicaRepository extends JpaRepository<SolicitudAcademica, Long>,
        JpaSpecificationExecutor<SolicitudAcademica> {

    List<SolicitudAcademica> findByEstado(EstadoSolicitud estado);
    List<SolicitudAcademica> findByTipoSolicitud(TipoSolicitud tipoSolicitud);
    List<SolicitudAcademica> findByPrioridad(PrioridadSolicitud prioridad);
    List<SolicitudAcademica> findByResponsableAsignado_Id(Long responsableId);
}