package co.edu.uniquindio.proyectoprogramacion.repositories.spec;

import co.edu.uniquindio.proyectoprogramacion.model.SolicitudAcademica;
import org.springframework.data.jpa.domain.Specification;

public class SolicitudSpecifications {

    private SolicitudSpecifications() {
    }

    public static Specification<SolicitudAcademica> conEstado(String estado) {
        return (root, query, cb) -> {
            if (estado == null || estado.isBlank()) return cb.conjunction();
            return cb.equal(cb.upper(root.get("estado").as(String.class)), estado.trim().toUpperCase());
        };
    }

    public static Specification<SolicitudAcademica> conTipo(String tipo) {
        return (root, query, cb) -> {
            if (tipo == null || tipo.isBlank()) return cb.conjunction();
            return cb.equal(cb.upper(root.get("tipoSolicitud").as(String.class)), tipo.trim().toUpperCase());
        };
    }

    public static Specification<SolicitudAcademica> conPrioridad(String prioridad) {
        return (root, query, cb) -> {
            if (prioridad == null || prioridad.isBlank()) return cb.conjunction();
            return cb.equal(cb.upper(root.get("prioridad").as(String.class)), prioridad.trim().toUpperCase());
        };
    }

    public static Specification<SolicitudAcademica> conResponsableId(Long responsableId) {
        return (root, query, cb) -> {
            if (responsableId == null) return cb.conjunction();
            return cb.equal(root.get("responsableAsignado").get("id"), responsableId);
        };
    }
}