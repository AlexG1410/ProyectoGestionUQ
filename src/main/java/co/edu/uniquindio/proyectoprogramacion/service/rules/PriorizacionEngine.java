package co.edu.uniquindio.proyectoprogramacion.service.rules;

import co.edu.uniquindio.proyectoprogramacion.model.entity.ReglaPriorizacion;
import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.repository.ReglaPriorizacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PriorizacionEngine {

    private final ReglaPriorizacionRepository reglaRepository;

    public ResultadoPriorizacion calcular(TipoSolicitud tipo, ImpactoAcademico impacto, LocalDate fechaLimite) {
        List<ReglaPriorizacion> reglas = reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(tipo, impacto);
        if (reglas.isEmpty()) {
            return new ResultadoPriorizacion(Prioridad.MEDIA, "Prioridad asignada por regla por defecto");
        }
        ReglaPriorizacion regla = reglas.get(0);
        
        // Manejar fechaLimite null: si no hay fecha límite, se asume plazo indefinido
        long dias = fechaLimite == null 
            ? Long.MAX_VALUE 
            : ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
        
        Prioridad prioridad = dias <= regla.getDiasAntesVence() ? regla.getPrioridadResultante() : Prioridad.MEDIA;
        return new ResultadoPriorizacion(prioridad, regla.getJustificacionPlantilla());
    }

    public record ResultadoPriorizacion(Prioridad prioridad, String justificacion) {}
}