package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import co.edu.uniquindio.proyectoprogramacion.service.ReglasSolicitudService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReglasSolicitudServiceImpl implements ReglasSolicitudService {

    @Override
    public Prioridad calcularPrioridad(Solicitud solicitud) {
        int puntaje = calcularPuntaje(
                solicitud.getTipoSolicitud(),
                solicitud.getImpactoAcademico(),
                solicitud.getFechaLimite()
        );

        if (puntaje >= 80) return Prioridad.CRITICA;
        if (puntaje >= 55) return Prioridad.ALTA;
        if (puntaje >= 30) return Prioridad.MEDIA;
        return Prioridad.BAJA;
    }

    @Override
    public String construirJustificacionPrioridad(Solicitud solicitud) {
        List<String> razones = construirRazones(
                solicitud.getTipoSolicitud(),
                solicitud.getImpactoAcademico(),
                solicitud.getFechaLimite()
        );
        return String.join("; ", razones);
    }

    @Override
    public SugerirClasificacionPrioridadResponseDTO sugerirClasificacionPrioridad(SugerirClasificacionPrioridadRequestDTO request) {
        TipoSolicitud tipo = sugerirTipo(request.getDescripcion());
        int puntaje = calcularPuntaje(tipo, request.getImpactoAcademico(), request.getFechaLimite());

        Prioridad prioridad =
                puntaje >= 80 ? Prioridad.CRITICA :
                        puntaje >= 55 ? Prioridad.ALTA :
                                puntaje >= 30 ? Prioridad.MEDIA :
                                        Prioridad.BAJA;

        List<String> razones = construirRazones(tipo, request.getImpactoAcademico(), request.getFechaLimite());
        razones.add("Clasificación sugerida a partir del texto descriptivo");
        razones.add("Puntaje total calculado: " + puntaje);

        return SugerirClasificacionPrioridadResponseDTO.builder()
                .tipoSolicitudSugerido(tipo)
                .prioridadSugerida(prioridad)
                .puntajeTotal(puntaje)
                .razones(razones)
                .requiereConfirmacionHumana(true)
                .build();
    }

    private int calcularPuntaje(TipoSolicitud tipoSolicitud, ImpactoAcademico impacto, LocalDate fechaLimite) {
        int puntaje = 0;

        if (impacto != null) {
            switch (impacto) {
                case CRITICO -> puntaje += 50;
                case ALTO -> puntaje += 35;
                case MEDIO -> puntaje += 20;
                case BAJO -> puntaje += 5;
            }
        }

        if (fechaLimite != null) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
            if (dias <= 2) puntaje += 35;
            else if (dias <= 7) puntaje += 25;
            else if (dias <= 15) puntaje += 10;
        }

        if (tipoSolicitud != null) {
            switch (tipoSolicitud) {
                case HOMOLOGACION, CANCELACION_ASIGNATURAS, SOLICITUD_CUPOS -> puntaje += 15;
                case REGISTRO_ASIGNATURAS -> puntaje += 12;
                case CONSULTA_ACADEMICA -> puntaje += 6;
                case OTRO -> puntaje += 3;
            }
        }

        return puntaje;
    }

    private List<String> construirRazones(TipoSolicitud tipoSolicitud, ImpactoAcademico impacto, LocalDate fechaLimite) {
        List<String> razones = new ArrayList<>();

        if (impacto != null) {
            razones.add("Impacto académico: " + impacto);
        }

        if (fechaLimite != null) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
            razones.add("Fecha límite a " + dias + " día(s)");
        }

        if (tipoSolicitud != null) {
            razones.add("Tipo de solicitud: " + tipoSolicitud);
        }

        return razones;
    }

    private TipoSolicitud sugerirTipo(String descripcion) {
        String texto = descripcion.toLowerCase();

        if (texto.contains("homolog") || texto.contains("equivalencia")) {
            return TipoSolicitud.HOMOLOGACION;
        }
        if (texto.contains("cancel") || texto.contains("retiro")) {
            return TipoSolicitud.CANCELACION_ASIGNATURAS;
        }
        if (texto.contains("cupo") || texto.contains("sobrecupo")) {
            return TipoSolicitud.SOLICITUD_CUPOS;
        }
        if (texto.contains("registro") || texto.contains("matrícula") || texto.contains("asignatura")) {
            return TipoSolicitud.REGISTRO_ASIGNATURAS;
        }
        if (texto.contains("consulta") || texto.contains("pregunta")) {
            return TipoSolicitud.CONSULTA_ACADEMICA;
        }

        return TipoSolicitud.OTRO;
    }
}