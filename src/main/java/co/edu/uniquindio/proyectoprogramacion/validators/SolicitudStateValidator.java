package co.edu.uniquindio.proyectoprogramacion.validators;

import co.edu.uniquindio.proyectoprogramacion.exception.BusinessRuleException;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.EstadoSolicitud;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SolicitudStateValidator {

    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TRANSICIONES = Map.of(
            EstadoSolicitud.REGISTRADA, Set.of(EstadoSolicitud.CLASIFICADA),
            EstadoSolicitud.CLASIFICADA, Set.of(EstadoSolicitud.EN_ATENCION),
            EstadoSolicitud.EN_ATENCION, Set.of(EstadoSolicitud.ATENDIDA),
            EstadoSolicitud.ATENDIDA, Set.of(EstadoSolicitud.CERRADA),
            EstadoSolicitud.CERRADA, Set.of()
    );

    public void validarTransicion(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        if (!TRANSICIONES.getOrDefault(actual, Set.of()).contains(nuevo)) {
            throw new BusinessRuleException("Transición inválida: " + actual + " -> " + nuevo);
        }
    }
}