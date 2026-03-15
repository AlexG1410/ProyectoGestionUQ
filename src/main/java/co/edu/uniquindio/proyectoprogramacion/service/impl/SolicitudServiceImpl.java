package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.*;
import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.*;
import co.edu.uniquindio.proyectoprogramacion.mapper.HistorialMapper;
import co.edu.uniquindio.proyectoprogramacion.mapper.SolicitudMapper;
import co.edu.uniquindio.proyectoprogramacion.mapper.UsuarioMapper;
import co.edu.uniquindio.proyectoprogramacion.model.entity.*;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import co.edu.uniquindio.proyectoprogramacion.repository.*;
import co.edu.uniquindio.proyectoprogramacion.repository.spec.SolicitudSpecification;
import co.edu.uniquindio.proyectoprogramacion.service.ReglasSolicitudService;
import co.edu.uniquindio.proyectoprogramacion.service.SolicitudService;
import co.edu.uniquindio.proyectoprogramacion.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialSolicitudRepository historialRepository;
    private final SecurityUtils securityUtils;
    private final SolicitudMapper solicitudMapper;
    private final UsuarioMapper usuarioMapper;
    private final HistorialMapper historialMapper;
    private final ReglasSolicitudService reglasSolicitudService;

    @Override
    public SolicitudResponseDTO crearSolicitud(SolicitudCreateDTO request) {
        Usuario solicitante = securityUtils.getUsuarioAutenticado();

        Solicitud solicitud = Solicitud.builder()
                .tipoSolicitud(request.getTipoSolicitud())
                .descripcion(request.getDescripcion())
                .canalOrigen(request.getCanalOrigen())
                .impactoAcademico(request.getImpactoAcademico())
                .fechaLimite(request.getFechaLimite())
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(solicitante)
                .cerrada(false)
                .fechaHoraRegistro(LocalDateTime.now())
                .build();

        solicitud = solicitudRepository.save(solicitud);

        registrarHistorial(
                solicitud,
                solicitante,
                AccionHistorial.REGISTRO_SOLICITUD,
                "Solicitud registrada",
                null,
                EstadoSolicitud.REGISTRADA,
                null,
                null
        );

        return solicitudMapper.toSolicitudResponseDTO(solicitud);
    }

    @Override
    public SolicitudResponseDTO clasificar(Long id, ClasificarSolicitudRequestDTO request) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        Solicitud solicitud = getSolicitud(id);
        validarNoCerrada(solicitud);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.setTipoSolicitud(request.getTipoSolicitud());

        if (solicitud.getEstado() == EstadoSolicitud.REGISTRADA) {
            solicitud.setEstado(EstadoSolicitud.CLASIFICADA);
        }

        solicitud = solicitudRepository.save(solicitud);

        registrarHistorial(
                solicitud,
                actor,
                AccionHistorial.CLASIFICACION_SOLICITUD,
                request.getObservacion(),
                estadoAnterior,
                solicitud.getEstado(),
                null,
                null
        );

        return solicitudMapper.toSolicitudResponseDTO(solicitud);
    }

    @Override
    public SolicitudResponseDTO priorizar(Long id, PriorizarSolicitudRequestDTO request) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        Solicitud solicitud = getSolicitud(id);
        validarNoCerrada(solicitud);

        if (solicitud.getTipoSolicitud() == null) {
            throw new BusinessRuleException("La solicitud debe estar clasificada antes de priorizar");
        }

        Prioridad prioridadAnterior = solicitud.getPrioridad();
        solicitud.setPrioridad(request.getPrioridad());
        solicitud.setJustificacionPrioridad(request.getJustificacionPrioridad());

        solicitud = solicitudRepository.save(solicitud);

        registrarHistorial(
                solicitud,
                actor,
                AccionHistorial.PRIORIZACION_SOLICITUD,
                request.getJustificacionPrioridad(),
                null,
                null,
                prioridadAnterior,
                solicitud.getPrioridad()
        );

        return solicitudMapper.toSolicitudResponseDTO(solicitud);
    }

    @Override
    public SolicitudResponseDTO asignar(Long id, AsignarResponsableRequestDTO request) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        Solicitud solicitud = getSolicitud(id);
        validarNoCerrada(solicitud);

        Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsable no encontrado"));

        if (!Boolean.TRUE.equals(responsable.getActivo())) {
            throw new BusinessRuleException("El responsable no está activo");
        }

        solicitud.setResponsable(responsable);
        solicitud = solicitudRepository.save(solicitud);

        registrarHistorial(
                solicitud,
                actor,
                AccionHistorial.ASIGNACION_RESPONSABLE,
                "Asignado a " + responsable.getUsername(),
                null,
                null,
                null,
                null
        );

        return solicitudMapper.toSolicitudResponseDTO(solicitud);
    }

    @Override
    public SolicitudResponseDTO iniciarAtencion(Long id, ObservacionRequestDTO request) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        Solicitud solicitud = getSolicitud(id);
        validarNoCerrada(solicitud);

        if (solicitud.getEstado() != EstadoSolicitud.CLASIFICADA) {
            throw new BusinessRuleException("Solo se puede iniciar atención desde estado CLASIFICADA");
        }

        EstadoSolicitud anterior = solicitud.getEstado();
        solicitud.setEstado(EstadoSolicitud.EN_ATENCION);
        solicitud = solicitudRepository.save(solicitud);

        registrarHistorial(
                solicitud,
                actor,
                AccionHistorial.INICIO_ATENCION,
                request != null ? request.getObservacion() : null,
                anterior,
                solicitud.getEstado(),
                null,
                null
        );

        return solicitudMapper.toSolicitudResponseDTO(solicitud);
    }

    @Override
    public SolicitudResponseDTO marcarAtendida(Long id, ObservacionRequestDTO request) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        Solicitud solicitud = getSolicitud(id);
        validarNoCerrada(solicitud);

        if (solicitud.getEstado() != EstadoSolicitud.EN_ATENCION) {
            throw new BusinessRuleException("Solo se puede marcar atendida desde estado EN_ATENCION");
        }

        EstadoSolicitud anterior = solicitud.getEstado();
        solicitud.setEstado(EstadoSolicitud.ATENDIDA);
        solicitud = solicitudRepository.save(solicitud);

        registrarHistorial(
                solicitud,
                actor,
                AccionHistorial.MARCAR_ATENDIDA,
                request != null ? request.getObservacion() : null,
                anterior,
                solicitud.getEstado(),
                null,
                null
        );

        return solicitudMapper.toSolicitudResponseDTO(solicitud);
    }

    @Override
    public SolicitudResponseDTO cerrar(Long id, CerrarSolicitudRequestDTO request) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        Solicitud solicitud = getSolicitud(id);
        validarNoCerrada(solicitud);

        if (solicitud.getEstado() != EstadoSolicitud.ATENDIDA) {
            throw new BusinessRuleException("Solo se puede cerrar una solicitud en estado ATENDIDA");
        }

        EstadoSolicitud anterior = solicitud.getEstado();
        solicitud.setEstado(EstadoSolicitud.CERRADA);
        solicitud.setCerrada(true);
        solicitud.setFechaCierre(LocalDateTime.now());
        solicitud.setObservacionCierre(request.getObservacionCierre());

        solicitud = solicitudRepository.save(solicitud);

        registrarHistorial(
                solicitud,
                actor,
                AccionHistorial.CIERRE_SOLICITUD,
                request.getObservacionCierre(),
                anterior,
                solicitud.getEstado(),
                null,
                null
        );

        return solicitudMapper.toSolicitudResponseDTO(solicitud);
    }

    @Override
    public SolicitudResponseDTO obtenerPorId(Long id) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);
        return solicitudMapper.toSolicitudResponseDTO(getSolicitud(id));
    }

    @Override
    public Page<SolicitudResponseDTO> consultarSolicitudes(
            EstadoSolicitud estado,
            TipoSolicitud tipoSolicitud,
            Prioridad prioridad,
            Long responsableId,
            Pageable pageable
    ) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        Specification<Solicitud> spec = Specification
                .where(SolicitudSpecification.conEstado(estado))
                .and(SolicitudSpecification.conTipo(tipoSolicitud))
                .and(SolicitudSpecification.conPrioridad(prioridad))
                .and(SolicitudSpecification.conResponsableId(responsableId));

        return solicitudRepository.findAll(spec, pageable)
                .map(solicitudMapper::toSolicitudResponseDTO);
    }

    @Override
    public Page<SolicitudResponseDTO> misSolicitudes(Pageable pageable) {
        Usuario usuario = securityUtils.getUsuarioAutenticado();
        return solicitudRepository.findBySolicitante(usuario, pageable)
                .map(solicitudMapper::toSolicitudResponseDTO);
    }

    @Override
    public SolicitudResponseDTO miSolicitudPorId(Long id) {
        Usuario usuario = securityUtils.getUsuarioAutenticado();
        Solicitud solicitud = getSolicitud(id);

        if (!solicitud.getSolicitante().getId().equals(usuario.getId())
                && usuario.getRol() == RolUsuario.ESTUDIANTE) {
            throw new UnauthorizedOperationException("No puedes consultar una solicitud que no es tuya");
        }

        return solicitudMapper.toSolicitudResponseDTO(solicitud);
    }

    @Override
    public List<HistorialResponseDTO> historial(Long id) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        Solicitud solicitud = getSolicitud(id);

        return historialRepository.findBySolicitudOrderByFechaHoraAsc(solicitud)
                .stream()
                .map(historialMapper::toHistorialResponseDTO)
                .toList();
    }

    @Override
    public List<UsuarioSimpleDTO> responsablesActivos() {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);

        return usuarioRepository.findByActivoTrueAndRolIn(List.of(RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR))
                .stream()
                .map(usuarioMapper::toUsuarioSimpleDTO)
                .toList();
    }

    @Override
    public SugerirClasificacionPrioridadResponseDTO sugerirClasificacionPrioridad(SugerirClasificacionPrioridadRequestDTO request) {
        Usuario actor = securityUtils.getUsuarioAutenticado();
        validarGestion(actor);
        return reglasSolicitudService.sugerirClasificacionPrioridad(request);
    }

    private Solicitud getSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
    }

    private void validarNoCerrada(Solicitud solicitud) {
        if (Boolean.TRUE.equals(solicitud.getCerrada()) || solicitud.getEstado() == EstadoSolicitud.CERRADA) {
            throw new BusinessRuleException("La solicitud está cerrada y no puede modificarse");
        }
    }

    private void validarGestion(Usuario usuario) {
        if (usuario.getRol() != RolUsuario.ADMINISTRATIVO
                && usuario.getRol() != RolUsuario.COORDINADOR) {
            throw new UnauthorizedOperationException("No tienes permisos para gestionar solicitudes");
        }
    }

    private void registrarHistorial(Solicitud solicitud,
                                    Usuario actor,
                                    AccionHistorial accion,
                                    String observacion,
                                    EstadoSolicitud estadoAnterior,
                                    EstadoSolicitud estadoNuevo,
                                    Prioridad prioridadAnterior,
                                    Prioridad prioridadNueva) {

        HistorialSolicitud historial = HistorialSolicitud.builder()
                .solicitud(solicitud)
                .usuarioResponsable(actor)
                .accion(accion)
                .observaciones(observacion)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .prioridadAnterior(prioridadAnterior)
                .prioridadNueva(prioridadNueva)
                .fechaHora(LocalDateTime.now())
                .build();

        historialRepository.save(historial);
    }
}