package co.edu.uniquindio.proyectoprogramacion.services.rules;

import co.edu.uniquindio.proyectoprogramacion.dto.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrioridadRuleEngine {

    public SugerirPrioridadResponseDTO sugerir(SugerirPrioridadRequestDTO dto) {
        int puntaje = 0;
        List<String> razones = new ArrayList<>();

        // =========================
        // REGLA 1: Impacto académico
        // =========================
        String impacto = dto.getImpactoAcademico() == null ? "" : dto.getImpactoAcademico().trim().toUpperCase();

        switch (impacto) {
            case "CRITICO":
            case "CRÍTICO":
                puntaje += 50;
                razones.add("Impacto académico crítico (+50)");
                break;
            case "ALTO":
                puntaje += 35;
                razones.add("Impacto académico alto (+35)");
                break;
            case "MEDIO":
                puntaje += 20;
                razones.add("Impacto académico medio (+20)");
                break;
            case "BAJO":
                puntaje += 5;
                razones.add("Impacto académico bajo (+5)");
                break;
            default:
                razones.add("Sin impacto académico definido (+0)");
                break;
        }

        // =========================
        // REGLA 2: Fecha límite
        // =========================
        if (dto.getFechaLimite() != null && !dto.getFechaLimite().isBlank()) {
            try {
                LocalDate fechaLimite = LocalDate.parse(dto.getFechaLimite());
                long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);

                if (dias < 0) {
                    puntaje += 45;
                    razones.add("Fecha límite vencida (+45)");
                } else if (dias <= 2) {
                    puntaje += 40;
                    razones.add("Fecha límite muy próxima (<=2 días) (+40)");
                } else if (dias <= 7) {
                    puntaje += 25;
                    razones.add("Fecha límite próxima (<=7 días) (+25)");
                } else if (dias <= 15) {
                    puntaje += 10;
                    razones.add("Fecha límite moderadamente cercana (<=15 días) (+10)");
                } else {
                    razones.add("Fecha límite no urgente (+0)");
                }

            } catch (Exception e) {
                razones.add("Fecha límite inválida, no se evaluó (+0)");
            }
        } else {
            razones.add("Sin fecha límite definida (+0)");
        }

        // =========================
        // REGLA 3: Tipo de solicitud
        // =========================
        TipoSolicitud tipo = dto.getTipoSolicitud();
        if (tipo != null) {
            switch (tipo) {
                case SOLICITUD_CUPOS:
                    puntaje += 20;
                    razones.add("Tipo SOLICITUD_CUPOS (+20)");
                    break;
                case CANCELACION_ASIGNATURAS:
                    puntaje += 18;
                    razones.add("Tipo CANCELACION_ASIGNATURAS (+18)");
                    break;
                case HOMOLOGACION:
                    puntaje += 15;
                    razones.add("Tipo HOMOLOGACION (+15)");
                    break;
                case REGISTRO_ASIGNATURAS:
                    puntaje += 12;
                    razones.add("Tipo REGISTRO_ASIGNATURAS (+12)");
                    break;
                case CONSULTA_ACADEMICA:
                    puntaje += 5;
                    razones.add("Tipo CONSULTA_ACADEMICA (+5)");
                    break;
                case OTRO:
                default:
                    puntaje += 3;
                    razones.add("Tipo OTRO (+3)");
                    break;
            }
        }

        // =========================
        // MAPEO PUNTAJE -> PRIORIDAD
        // =========================
        String prioridadSugerida;
        if (puntaje >= 80) {
            prioridadSugerida = "CRITICA";
        } else if (puntaje >= 50) {
            prioridadSugerida = "ALTA";
        } else if (puntaje >= 25) {
            prioridadSugerida = "MEDIA";
        } else {
            prioridadSugerida = "BAJA";
        }

        razones.add("Puntaje total: " + puntaje + " => Prioridad sugerida: " + prioridadSugerida);

        return SugerirPrioridadResponseDTO.builder()
                .prioridadSugerida(prioridadSugerida)
                .puntajeTotal(puntaje)
                .razones(razones)
                .build();
    }
}