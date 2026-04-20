package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.*;
import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.BusinessException;
import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.mapper.HistorialMapper;
import co.edu.uniquindio.proyectoprogramacion.mapper.SolicitudMapper;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.AccionHistorial;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.repository.HistorialSolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudSpecification;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.service.HistorialService;
import co.edu.uniquindio.proyectoprogramacion.service.IAService;
import co.edu.uniquindio.proyectoprogramacion.service.SolicitudService;
import co.edu.uniquindio.proyectoprogramacion.service.rules.AuthorizationPolicy;
import co.edu.uniquindio.proyectoprogramacion.service.rules.EstadoMachine;
import co.edu.uniquindio.proyectoprogramacion.service.rules.PriorizacionEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialSolicitudRepository historialRepository;
    private final SolicitudMapper solicitudMapper;
    private final HistorialMapper historialMapper;
    private final EstadoMachine estadoMachine;
    private final AuthorizationPolicy authorizationPolicy;
    private final HistorialService historialService;
    private final IAService iaService;
    private final PriorizacionEngine priorizacionEngine;

    @Override
    public SolicitudResponseDTO registrar(SolicitudCreateDTO dto, UUID actorId) {
        Usuario actor = obtenerUsuario(actorId);
        authorizationPolicy.requireAny(actor.getRol(), RolUsuario.ESTUDIANTE, RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR);

        // SEGURIDAD (Prevención de Suplantación): El solicitante es siempre el usuario autenticado (JWT).
        // La identificación se extrae del SecurityContext, nunca del cuerpo de la petición.
        // Esto previene que alguien intente registrar una solicitud suplantando la identidad de otro usuario.
        Usuario solicitante = actor;

        Solicitud solicitud = Solicitud.builder()
                .tipoSolicitud(dto.getTipoSolicitud())
                .descripcion(dto.getDescripcion())
                .canalOrigen(dto.getCanalOrigen())
                .impactoAcademico(dto.getImpactoAcademico())
                .fechaLimite(dto.getFechaLimite())
                .fechaHoraRegistro(LocalDateTime.now())
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(solicitante)
                .build();

        solicitud = solicitudRepository.save(solicitud);
        historialService.registrar(solicitud.getId(), actorId, AccionHistorial.REGISTRO_SOLICITUD, "Registro inicial de la solicitud", null);
        return solicitudMapper.toResponse(solicitud);
    }
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> consultar(FiltroSolicitudesDTO filtro) {
        return solicitudRepository.findAll(SolicitudSpecification.conFiltros(
                        filtro.getEstado(), filtro.getTipo(), filtro.getPrioridad(), filtro.getResponsableId()))
                .stream()
                .map(solicitudMapper::toResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public SolicitudResponseDTO obtenerPorId(UUID solicitudId, UUID usuarioId, RolUsuario rol) {
        Solicitud solicitud = obtenerSolicitud(solicitudId);
        
        // Validar propiedad para ESTUDIANTE
        if (RolUsuario.ESTUDIANTE.equals(rol) && !solicitud.getSolicitante().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId);
        }
        
        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponseDTO> obtenerMisSolicitudes(UUID solicitanteId, FiltroSolicitudesDTO filtro) {
        return solicitudRepository.findBySolicitanteId(solicitanteId)
                .stream()
                .filter(s -> {
                    if (filtro.getEstado() != null && !s.getEstado().equals(filtro.getEstado())) {
                        return false;
                    }
                    if (filtro.getTipo() != null && !s.getTipoSolicitud().equals(filtro.getTipo())) {
                        return false;
                    }
                    if (filtro.getPrioridad() != null && !s.getPrioridad().equals(filtro.getPrioridad())) {
                        return false;
                    }
                    return true;
                })
                .map(solicitudMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponseDTO obtenerMiSolicitud(UUID solicitudId, UUID solicitanteId) {
        Solicitud solicitud = obtenerSolicitud(solicitudId);
        if (!solicitud.getSolicitante().getId().equals(solicitanteId)) {
            throw new ResourceNotFoundException("Solicitud no encontrada o no pertenece al usuario autenticado");
        }
        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    public SolicitudResponseDTO clasificarPriorizar(UUID solicitudId, ClasificarPriorizarDTO dto, UUID actorId) {
        Usuario actor = obtenerUsuario(actorId);
        authorizationPolicy.requireAny(actor.getRol(), RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR);

        Solicitud solicitud = obtenerSolicitud(solicitudId);
        solicitud.validarModificable();

        solicitud.setTipoSolicitud(dto.getTipoSolicitud());

        // Integración con PriorizacionEngine: calcular prioridad automáticamente por reglas
        if (dto.getPrioridad() == null || (dto.getJustificacionPrioridad() == null || dto.getJustificacionPrioridad().isEmpty())) {
            // Calcular automáticamente usando reglas
            PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
                    dto.getTipoSolicitud(),
                    solicitud.getImpactoAcademico(),
                    solicitud.getFechaLimite()
            );
            solicitud.setPrioridad(resultado.prioridad());
            solicitud.setJustificacionPrioridad(resultado.justificacion());
        } else {
            // Usar valores explícitos proporcionados por el usuario (override fallback)
            solicitud.setPrioridad(dto.getPrioridad());
            solicitud.setJustificacionPrioridad(dto.getJustificacionPrioridad());
        }

        if (!EstadoSolicitud.CLASIFICADA.equals(solicitud.getEstado())) {
            estadoMachine.validar(solicitud.getEstado(), EstadoSolicitud.CLASIFICADA);
            solicitud.setEstado(EstadoSolicitud.CLASIFICADA);
        }

        solicitudRepository.save(solicitud);
        historialService.registrar(solicitudId, actorId, AccionHistorial.PRIORIZACION_SOLICITUD,
                "Clasificación y priorización", solicitud.getJustificacionPrioridad());
        return solicitudMapper.toResponse(solicitud);
    }
    @Override
    public SolicitudResponseDTO asignarResponsable(UUID solicitudId, AsignarResponsableDTO dto, UUID actorId) {
        Usuario actor = obtenerUsuario(actorId);
        authorizationPolicy.requireAny(actor.getRol(), RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR);

        Solicitud solicitud = obtenerSolicitud(solicitudId);
        solicitud.validarModificable();

        Usuario responsable = obtenerUsuario(dto.getResponsableId());
        if (!responsable.isActivo()) {
            throw new BusinessException("El responsable seleccionado está inactivo");
        }
        if (!List.of(RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR)
                .contains(responsable.getRol())) {
            throw new BusinessException("El responsable no tiene un rol autorizado para atender solicitudes");
        }

        solicitud.setResponsable(responsable);
        solicitudRepository.save(solicitud);

        historialService.registrar(solicitudId, actorId, AccionHistorial.ASIGNACION_RESPONSABLE,
                "Asignación de responsable", "Responsable asignado: " + responsable.getUsername());
        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    public SolicitudResponseDTO iniciarAtencion(UUID solicitudId, String observacion, UUID actorId) {
        Usuario actor = obtenerUsuario(actorId);
        authorizationPolicy.requireAny(actor.getRol(), RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR);

        Solicitud solicitud = obtenerSolicitud(solicitudId);
        solicitud.validarModificable();

        if (!EstadoSolicitud.CLASIFICADA.equals(solicitud.getEstado())) {
            throw new BusinessException("Solo se pueden atender solicitudes clasificadas");
        }

        if (solicitud.getResponsable() == null) {
            throw new BusinessException("La solicitud no tiene responsable asignado");
        }

        estadoMachine.validar(solicitud.getEstado(), EstadoSolicitud.EN_ATENCION);
        solicitud.setEstado(EstadoSolicitud.EN_ATENCION);
        solicitudRepository.save(solicitud);

        historialService.registrar(solicitudId, actorId, AccionHistorial.INICIO_ATENCION,
                "Inicio de atención", observacion);
        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    public SolicitudResponseDTO marcarAtendida(UUID solicitudId, MarcarAtendidoDTO dto, UUID actorId) {
        Usuario actor = obtenerUsuario(actorId);
        authorizationPolicy.requireAny(actor.getRol(), RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR);

        Solicitud solicitud = obtenerSolicitud(solicitudId);
        solicitud.validarModificable();

        if (!EstadoSolicitud.EN_ATENCION.equals(solicitud.getEstado())) {
            throw new BusinessException("Solo se pueden marcar como atendidas las solicitudes en atención");
        }

        estadoMachine.validar(solicitud.getEstado(), EstadoSolicitud.ATENDIDA);
        solicitud.setEstado(EstadoSolicitud.ATENDIDA);
        solicitudRepository.save(solicitud);

        historialService.registrar(solicitudId, actorId, AccionHistorial.MARCAR_ATENDIDA,
                "Solicitud marcada como atendida", dto.getObservacion());
        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    public SolicitudResponseDTO cerrar(UUID solicitudId, CerrarSolicitudDTO dto, UUID actorId) {
        Usuario actor = obtenerUsuario(actorId);
        authorizationPolicy.requireAny(actor.getRol(), RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR);

        Solicitud solicitud = obtenerSolicitud(solicitudId);
        solicitud.validarModificable();

        if (!EstadoSolicitud.ATENDIDA.equals(solicitud.getEstado())) {
            throw new BusinessException("Solo se pueden cerrar las solicitudes atendidas");
        }

        estadoMachine.validar(solicitud.getEstado(), EstadoSolicitud.CERRADA);
        solicitud.setEstado(EstadoSolicitud.CERRADA);
        solicitud.setFechaCierre(LocalDateTime.now());
        solicitud.setObservacionCierre(dto.getObservacionCierre());
        solicitudRepository.save(solicitud);

        historialService.registrar(solicitudId, actorId, AccionHistorial.CIERRE_SOLICITUD,
                "Solicitud cerrada", dto.getObservacionCierre());
        return solicitudMapper.toResponse(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> historial(UUID solicitudId, UUID usuarioId, RolUsuario rol) {
        Solicitud solicitud = obtenerSolicitud(solicitudId);
        
        // Validar propiedad para ESTUDIANTE
        if (RolUsuario.ESTUDIANTE.equals(rol) && !solicitud.getSolicitante().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId);
        }

        return historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId)
                .stream()
                .map(historialMapper::toResponse)
                .toList();
    }

    private Usuario obtenerUsuario(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
    }

    private Solicitud obtenerSolicitud(UUID solicitudId) {
        return solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + solicitudId));
    }
}