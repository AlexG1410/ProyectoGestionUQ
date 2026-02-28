package co.edu.uniquindio.proyectoprogramacion.validators;

import co.edu.uniquindio.proyectoprogramacion.exceptions.BusinessException;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
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
            throw new BusinessException("Transición inválida: " + actual + " -> " + nuevo);
        }
    }
}