package co.edu.uniquindio.proyectoprogramacion.services.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.*;
import co.edu.uniquindio.proyectoprogramacion.repositories.spec.SolicitudSpecifications;
import org.springframework.data.jpa.domain.Specification;
import co.edu.uniquindio.proyectoprogramacion.mappers.SolicitudMapper;
import co.edu.uniquindio.proyectoprogramacion.mappers.HistorialMapper;
import co.edu.uniquindio.proyectoprogramacion.exceptions.BusinessException;
import co.edu.uniquindio.proyectoprogramacion.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.model.*;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.repositories.HistorialSolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repositories.SolicitudAcademicaRepository;
import co.edu.uniquindio.proyectoprogramacion.repositories.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.services.SolicitudService;
import co.edu.uniquindio.proyectoprogramacion.validators.SolicitudStateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudAcademicaRepository solicitudRepository;
    private final HistorialSolicitudRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudStateValidator stateValidator;
    private final SolicitudMapper solicitudMapper;
    private final HistorialMapper historialMapper;

    @Override
    public SolicitudResponseDTO registrar(SolicitudCreateDTO dto, String usuarioActor) {
        SolicitudAcademica solicitud = SolicitudAcademica.builder()
                .tipoSolicitud(dto.getTipoSolicitud())
                .descripcion(dto.getDescripcion())
                .canalOrigen(dto.getCanalOrigen())
                .fechaHoraRegistro(LocalDateTime.now())
                .identificacionSolicitante(dto.getIdentificacionSolicitante())
                .estado(EstadoSolicitud.REGISTRADA)
                .cerrada(false)
                .impactoAcademico(dto.getImpactoAcademico())
                .build();

        if (dto.getFechaLimite() != null && !dto.getFechaLimite().isBlank()) {
            solicitud.setFechaLimite(LocalDate.parse(dto.getFechaLimite()));
        }

        solicitud = solicitudRepository.save(solicitud);

        registrarHistorial(solicitud, "REGISTRO", usuarioActor, "Solicitud registrada");

        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    public SolicitudResponseDTO clasificarPriorizar(Long solicitudId, ClasificarPriorizarDTO dto, String usuarioActor) {
        SolicitudAcademica solicitud = getSolicitud(solicitudId);
        validarNoCerrada(solicitud);

        // si está registrada, se permite pasar a clasificada
        if (solicitud.getEstado() == EstadoSolicitud.REGISTRADA) {
            stateValidator.validarTransicion(solicitud.getEstado(), EstadoSolicitud.CLASIFICADA);
            solicitud.setEstado(EstadoSolicitud.CLASIFICADA);
        }

        solicitud.setTipoSolicitud(dto.getTipoSolicitud());
        solicitud.setPrioridad(dto.getPrioridad());
        solicitud.setJustificacionPrioridad(dto.getJustificacionPrioridad());

        solicitudRepository.save(solicitud);

        registrarHistorial(solicitud, "CLASIFICACION_PRIORIZACION", usuarioActor,
                "Tipo=" + dto.getTipoSolicitud() + ", Prioridad=" + dto.getPrioridad());

        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    public SolicitudResponseDTO asignarResponsable(Long solicitudId, AsignarResponsableDTO dto, String usuarioActor) {
        SolicitudAcademica solicitud = getSolicitud(solicitudId);
        validarNoCerrada(solicitud);

        Usuario responsable = usuarioRepository.findById(dto.getResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsable no encontrado"));

        if (!responsable.isActivo()) {
            throw new BusinessException("No se puede asignar un responsable inactivo");
        }
        if (responsable.getRol() == null ||
                !(responsable.getRol().name().equals("ADMINISTRATIVO") ||
                        responsable.getRol().name().equals("COORDINADOR"))) {
            throw new BusinessException("Solo se puede asignar a usuarios con rol ADMINISTRATIVO o COORDINADOR");
        }

        solicitud.setResponsableAsignado(responsable);
        solicitudRepository.save(solicitud);

        registrarHistorial(solicitud, "ASIGNACION", usuarioActor,
                "Asignado a " + responsable.getUsername());

        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    public SolicitudResponseDTO cambiarEstado(Long solicitudId, CambiarEstadoDTO dto, String usuarioActor) {
        SolicitudAcademica solicitud = getSolicitud(solicitudId);
        validarNoCerrada(solicitud);

        if (dto.getNuevoEstado() == EstadoSolicitud.CERRADA) {
            throw new BusinessException("Use el endpoint de cierre para cerrar una solicitud");
        }

        stateValidator.validarTransicion(solicitud.getEstado(), dto.getNuevoEstado());
        solicitud.setEstado(dto.getNuevoEstado());
        solicitudRepository.save(solicitud);

        registrarHistorial(solicitud, "CAMBIO_ESTADO", usuarioActor,
                (dto.getObservacion() == null ? "" : dto.getObservacion()));

        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    public SolicitudResponseDTO cerrarSolicitud(Long solicitudId, CerrarSolicitudDTO dto, String usuarioActor) {
        SolicitudAcademica solicitud = getSolicitud(solicitudId);

        if (solicitud.isCerrada() || solicitud.getEstado() == EstadoSolicitud.CERRADA) {
            throw new BusinessException("La solicitud ya está cerrada");
        }

        if (solicitud.getEstado() != EstadoSolicitud.ATENDIDA) {
            throw new BusinessException("Solo se puede cerrar una solicitud que esté en estado ATENDIDA");
        }

        stateValidator.validarTransicion(solicitud.getEstado(), EstadoSolicitud.CERRADA);
        solicitud.setEstado(EstadoSolicitud.CERRADA);
        solicitud.setCerrada(true);
        solicitudRepository.save(solicitud);

        registrarHistorial(solicitud, "CIERRE", usuarioActor, dto.getObservacionCierre());

        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponseDTO obtenerPorId(Long id) {
        return solicitudMapper.toResponse(getSolicitud(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> consultar(String estado, String tipo, String prioridad, Long responsableId) {

        Specification<SolicitudAcademica> spec = Specification
                .where(SolicitudSpecifications.conEstado(estado))
                .and(SolicitudSpecifications.conTipo(tipo))
                .and(SolicitudSpecifications.conPrioridad(prioridad))
                .and(SolicitudSpecifications.conResponsableId(responsableId));

        return solicitudRepository.findAll(spec).stream()
                .sorted(Comparator.comparing(SolicitudAcademica::getFechaHoraRegistro).reversed())
                .map(solicitudMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> obtenerHistorial(Long solicitudId) {
        getSolicitud(solicitudId); // valida existencia
        return historialRepository.findBySolicitud_IdOrderByFechaHoraAsc(solicitudId)
                .stream()
                .map(historialMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String generarResumenIA(Long solicitudId) {
        // RF-11: sistema funcional sin IA real
        SolicitudAcademica s = getSolicitud(solicitudId);
        int eventos = historialRepository.findBySolicitud_IdOrderByFechaHoraAsc(solicitudId).size();

        return "Resumen (modo asistente sin IA externa): Solicitud #" + s.getId() +
                " en estado " + s.getEstado() +
                ", tipo " + s.getTipoSolicitud() +
                ", prioridad " + (s.getPrioridad() == null ? "SIN DEFINIR" : s.getPrioridad()) +
                ", eventos de historial: " + eventos + ".";
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private SolicitudAcademica getSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con id " + id));
    }

    private void validarNoCerrada(SolicitudAcademica solicitud) {
        if (solicitud.isCerrada() || solicitud.getEstado() == EstadoSolicitud.CERRADA) {
            throw new BusinessException("La solicitud está cerrada y no puede modificarse");
        }
    }

    private void registrarHistorial(SolicitudAcademica solicitud, String accion, String usuarioActor, String observaciones) {
        HistorialSolicitud h = HistorialSolicitud.builder()
                .solicitud(solicitud)
                .fechaHora(LocalDateTime.now())
                .accion(accion)
                .usuarioResponsable(usuarioActor)
                .observaciones(observaciones)
                .build();
        historialRepository.save(h);
    }

}