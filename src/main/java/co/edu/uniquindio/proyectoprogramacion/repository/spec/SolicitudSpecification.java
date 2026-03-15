package co.edu.uniquindio.proyectoprogramacion.repository.spec;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import org.springframework.data.jpa.domain.Specification;

public class SolicitudSpecification {

    public static Specification<Solicitud> conEstado(EstadoSolicitud estado) {
        return (root, query, cb) ->
                estado == null ? null : cb.equal(root.get("estado"), estado);
    }

    public static Specification<Solicitud> conTipo(TipoSolicitud tipoSolicitud) {
        return (root, query, cb) ->
                tipoSolicitud == null ? null : cb.equal(root.get("tipoSolicitud"), tipoSolicitud);
    }

    public static Specification<Solicitud> conPrioridad(Prioridad prioridad) {
        return (root, query, cb) ->
                prioridad == null ? null : cb.equal(root.get("prioridad"), prioridad);
    }

    public static Specification<Solicitud> conResponsableId(Long responsableId) {
        return (root, query, cb) ->
                responsableId == null ? null : cb.equal(root.get("responsable").get("id"), responsableId);
    }
}