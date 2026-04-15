package co.edu.uniquindio.proyectoprogramacion.service.rules;

import co.edu.uniquindio.proyectoprogramacion.repository.TransicionEstadoRepository;
import co.edu.uniquindio.proyectoprogramacion.exception.BusinessException;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstadoMachine {

    private final TransicionEstadoRepository transicionRepository;

    public void validar(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        boolean permitida = transicionRepository.findByDesdeAndHaciaAndPermitidaTrue(actual, nuevo).isPresent();
        if (!permitida) {
            throw new BusinessException("La transición de estado no es válida");
        }
    }
}