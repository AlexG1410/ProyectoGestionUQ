package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

public final class SolicitudSpecification {
    private SolicitudSpecification() {}

    public static Specification<Solicitud> conFiltros(EstadoSolicitud estado, TipoSolicitud tipo, Prioridad prioridad,
                                                      UUID responsableId, String descripcion, String identificacionSolicitante) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (estado != null) predicates = cb.and(predicates, cb.equal(root.get("estado"), estado));
            if (tipo != null) predicates = cb.and(predicates, cb.equal(root.get("tipoSolicitud"), tipo));
            if (prioridad != null) predicates = cb.and(predicates, cb.equal(root.get("prioridad"), prioridad));
            if (responsableId != null) predicates = cb.and(predicates, cb.equal(root.get("responsable").get("id"), responsableId));
            if (descripcion != null && !descripcion.isBlank()) {
                predicates = cb.and(predicates, cb.like(cb.lower(root.get("descripcion")), "%" + descripcion.toLowerCase() + "%"));
            }
            if (identificacionSolicitante != null && !identificacionSolicitante.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("solicitante").get("identificacion"), identificacionSolicitante));
            }
            return predicates;
        };
    }

    public static Specification<Solicitud> conFiltrosSolicitante(UUID solicitanteId, EstadoSolicitud estado, TipoSolicitud tipo, Prioridad prioridad) {
        return (root, query, cb) -> {
            var predicates = cb.and(cb.equal(root.get("solicitante").get("id"), solicitanteId));
            if (estado != null) predicates = cb.and(predicates, cb.equal(root.get("estado"), estado));
            if (tipo != null) predicates = cb.and(predicates, cb.equal(root.get("tipoSolicitud"), tipo));
            if (prioridad != null) predicates = cb.and(predicates, cb.equal(root.get("prioridad"), prioridad));
            return predicates;
        };
    }
}
