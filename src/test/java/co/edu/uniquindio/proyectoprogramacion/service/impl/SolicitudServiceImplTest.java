package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.*;
import co.edu.uniquindio.proyectoprogramacion.dto.historial.HistorialResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.BusinessException;
import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.mapper.HistorialMapper;
import co.edu.uniquindio.proyectoprogramacion.mapper.SolicitudMapper;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.*;
import co.edu.uniquindio.proyectoprogramacion.repository.HistorialSolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.service.HistorialService;
import co.edu.uniquindio.proyectoprogramacion.service.IAService;
import co.edu.uniquindio.proyectoprogramacion.service.rules.AuthorizationPolicy;
import co.edu.uniquindio.proyectoprogramacion.service.rules.EstadoMachine;
import co.edu.uniquindio.proyectoprogramacion.service.rules.PriorizacionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitudServiceImpl Tests")
class SolicitudServiceImplTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistorialSolicitudRepository historialRepository;

    @Mock
    private SolicitudMapper solicitudMapper;

    @Mock
    private HistorialMapper historialMapper;

    @Mock
    private EstadoMachine estadoMachine;

    @Mock
    private AuthorizationPolicy authorizationPolicy;

    @Mock
    private HistorialService historialService;

    @Mock
    private IAService iaService;

    @Mock
    private PriorizacionEngine priorizacionEngine;

    private SolicitudServiceImpl solicitudService;
    private UUID actorId;
    private UUID solicitanteId;
    private UUID solicitudId;
    private UUID responsableId;

    @BeforeEach
    void setUp() {
        solicitudService = new SolicitudServiceImpl(
                solicitudRepository, usuarioRepository, historialRepository,
                solicitudMapper, historialMapper, estadoMachine, authorizationPolicy,
                historialService, iaService, priorizacionEngine
        );

        actorId = UUID.randomUUID();
        solicitanteId = UUID.randomUUID();
        solicitudId = UUID.randomUUID();
        responsableId = UUID.randomUUID();
    }

    // ============= REGISTRAR SOLICITUD TESTS =============

    @Test
    @DisplayName("registrar debe registrar correctamente cuando el actor es ESTUDIANTE")
    void testRegistrar_ActorEstudiante_Success() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "juan", RolUsuario.ESTUDIANTE);
        SolicitudCreateDTO dto = crearSolicitudCreateDTO("Necesito cambio de calificación");

        Solicitud solicitudGuardada = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, actor);
        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);
        when(solicitudMapper.toResponse(solicitudGuardada)).thenReturn(responseDTO);

        // Act
        SolicitudResponseDTO result = solicitudService.registrar(dto, actorId);

        // Assert
        assertNotNull(result);
        verify(authorizationPolicy).requireAny(actor.getRol(), RolUsuario.ESTUDIANTE, RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR);
        verify(solicitudRepository).save(argThat(s -> 
            s.getTipoSolicitud().equals(dto.getTipoSolicitud()) &&
            s.getEstado().equals(EstadoSolicitud.REGISTRADA) &&
            s.getSolicitante().equals(actor)
        ));
        verify(solicitudMapper).toResponse(solicitudGuardada);
        verify(historialService).registrar(solicitudId, actorId, AccionHistorial.REGISTRO_SOLICITUD, "Registro inicial de la solicitud", null);
    }

    @Test
    @DisplayName("registrar debe guardar la solicitud en estado REGISTRADA")
    void testRegistrar_ShouldSetEstadoRegistrada() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        SolicitudCreateDTO dto = crearSolicitudCreateDTO("Nueva solicitud");

        Solicitud solicitudCapturada = new Solicitud();
        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(invocation -> {
            Solicitud s = invocation.getArgument(0);
            solicitudCapturada.setEstado(s.getEstado());
            return s;
        });
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        // Act
        solicitudService.registrar(dto, actorId);

        // Assert
        assertEquals(EstadoSolicitud.REGISTRADA, solicitudCapturada.getEstado());
    }

    @Test
    @DisplayName("registrar debe invocar historialService con acción REGISTRO_SOLICITUD")
    void testRegistrar_ShouldInvokeHistorialService() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "estudiante1", RolUsuario.ESTUDIANTE);
        SolicitudCreateDTO dto = crearSolicitudCreateDTO("Solicito cambio de nota");

        Solicitud solicitudGuardada = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, actor);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        // Act
        solicitudService.registrar(dto, actorId);

        // Assert
        verify(historialService).registrar(
            solicitudId,
            actorId,
            AccionHistorial.REGISTRO_SOLICITUD,
            "Registro inicial de la solicitud",
            null
        );
    }

    @Test
    @DisplayName("registrar debe retornar SolicitudResponseDTO mapeado")
    void testRegistrar_ShouldReturnMappedResponse() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "user", RolUsuario.COORDINADOR);
        SolicitudCreateDTO dto = crearSolicitudCreateDTO("descripción");

        Solicitud solicitudGuardada = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, actor);
        SolicitudResponseDTO expectedResponse = new SolicitudResponseDTO();
        expectedResponse.setId(solicitudId);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudGuardada);
        when(solicitudMapper.toResponse(solicitudGuardada)).thenReturn(expectedResponse);

        // Act
        SolicitudResponseDTO result = solicitudService.registrar(dto, actorId);

        // Assert
        assertEquals(expectedResponse, result);
        assertEquals(solicitudId, result.getId());
    }

    @Test
    @DisplayName("registrar debe usar el actor autenticado como solicitante")
    void testRegistrar_ShouldUseSolicitanteFromActor() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "juan", RolUsuario.ESTUDIANTE);
        SolicitudCreateDTO dto = crearSolicitudCreateDTO("solicitud");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, actor));
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        ArgumentCaptor<Solicitud> solicitudCaptor = ArgumentCaptor.forClass(Solicitud.class);

        // Act
        solicitudService.registrar(dto, actorId);

        // Assert
        verify(solicitudRepository).save(solicitudCaptor.capture());
        assertEquals(actor, solicitudCaptor.getValue().getSolicitante());
    }

    // ============= CLASIFICAR Y PRIORIZAR TESTS =============

    @Test
    @DisplayName("clasificarPriorizar debe permitir clasificar cuando el actor es ADMINISTRATIVO")
    void testClasificarPriorizar_ActorAdministrativo_Success() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);
        ClasificarPriorizarDTO dto = crearClasificarPriorizarDTO(Prioridad.ALTA, "Justificación alta prioridad");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(solicitud)).thenReturn(new SolicitudResponseDTO());

        // Act
        SolicitudResponseDTO result = solicitudService.clasificarPriorizar(solicitudId, dto, actorId);

        // Assert
        assertNotNull(result);
        verify(authorizationPolicy).requireAny(actor.getRol(), RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR);
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("clasificarPriorizar debe usar valores explícitos cuando vienen en DTO")
    void testClasificarPriorizar_ShouldUseExplicitValues() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);
        solicitud.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);
        solicitud.setImpactoAcademico(ImpactoAcademico.ALTO);

        ClasificarPriorizarDTO dto = crearClasificarPriorizarDTO(Prioridad.CRITICA, "Justificación personalizada");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        ArgumentCaptor<Solicitud> solicitudCaptor = ArgumentCaptor.forClass(Solicitud.class);

        // Act
        solicitudService.clasificarPriorizar(solicitudId, dto, actorId);

        // Assert
        verify(solicitudRepository).save(solicitudCaptor.capture());
        Solicitud captured = solicitudCaptor.getValue();
        assertEquals(Prioridad.CRITICA, captured.getPrioridad());
        assertEquals("Justificación personalizada", captured.getJustificacionPrioridad());
        verify(priorizacionEngine, never()).calcular(any(), any(), any());
    }

    @Test
    @DisplayName("clasificarPriorizar debe usar PriorizacionEngine cuando no vienen prioridad ni justificación")
    void testClasificarPriorizar_ShouldUsePriorizacionEngine() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);
        solicitud.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);
        solicitud.setImpactoAcademico(ImpactoAcademico.BAJO);

        ClasificarPriorizarDTO dto = new ClasificarPriorizarDTO();
        dto.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);
        dto.setPrioridad(null);
        dto.setJustificacionPrioridad(null);

        PriorizacionEngine.ResultadoPriorizacion resultado = new PriorizacionEngine.ResultadoPriorizacion(
            Prioridad.MEDIA,
            "Prioridad calculada por reglas"
        );

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.BAJO,
            null
        )).thenReturn(resultado);
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        ArgumentCaptor<Solicitud> solicitudCaptor = ArgumentCaptor.forClass(Solicitud.class);

        // Act
        solicitudService.clasificarPriorizar(solicitudId, dto, actorId);

        // Assert
        verify(priorizacionEngine).calcular(TipoSolicitud.CONSULTA_ACADEMICA, ImpactoAcademico.BAJO, null);
        verify(solicitudRepository).save(solicitudCaptor.capture());
        Solicitud captured = solicitudCaptor.getValue();
        assertEquals(Prioridad.MEDIA, captured.getPrioridad());
        assertEquals("Prioridad calculada por reglas", captured.getJustificacionPrioridad());
    }

    @Test
    @DisplayName("clasificarPriorizar debe cambiar estado a CLASIFICADA")
    void testClasificarPriorizar_ShouldChangeEstadoToClasificada() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "est", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);
        solicitud.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);

        ClasificarPriorizarDTO dto = crearClasificarPriorizarDTO(Prioridad.MEDIA, "Justificación");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        ArgumentCaptor<Solicitud> solicitudCaptor = ArgumentCaptor.forClass(Solicitud.class);

        // Act
        solicitudService.clasificarPriorizar(solicitudId, dto, actorId);

        // Assert
        verify(solicitudRepository).save(solicitudCaptor.capture());
        assertEquals(EstadoSolicitud.CLASIFICADA, solicitudCaptor.getValue().getEstado());
        verify(historialService).registrar(
            solicitudId,
            actorId,
            AccionHistorial.PRIORIZACION_SOLICITUD,
            "Clasificación y priorización",
            "Justificación"
        );
    }

    // ============= ASIGNAR RESPONSABLE TESTS =============

    @Test
    @DisplayName("asignarResponsable debe asignar correctamente un responsable activo con rol permitido")
    void testAsignarResponsable_ValidResponsable_Success() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario responsable = crearUsuarioActivo(responsableId, "consultor1", RolUsuario.CONSULTOR);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.CLASIFICADA, solicitante);
        AsignarResponsableDTO dto = new AsignarResponsableDTO();
        dto.setResponsableId(responsableId);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsable));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        // Act
        solicitudService.asignarResponsable(solicitudId, dto, actorId);

        // Assert
        verify(solicitudRepository).save(argThat(s -> responsable.equals(s.getResponsable())));
        verify(historialService).registrar(
            solicitudId,
            actorId,
            AccionHistorial.ASIGNACION_RESPONSABLE,
            "Asignación de responsable",
            "Responsable asignado: consultor1"
        );
    }

    @Test
    @DisplayName("asignarResponsable debe lanzar BusinessException si el responsable está inactivo")
    void testAsignarResponsable_InactiveResponsable_ThrowsException() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario responsableInactivo = crearUsuarioActivo(responsableId, "consultor_inactivo", RolUsuario.CONSULTOR);
        responsableInactivo.setActivo(false);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.CLASIFICADA, solicitante);
        AsignarResponsableDTO dto = new AsignarResponsableDTO();
        dto.setResponsableId(responsableId);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsableInactivo));

        // Act & Assert
        assertThrows(BusinessException.class, () ->
            solicitudService.asignarResponsable(solicitudId, dto, actorId),
            "El responsable seleccionado está inactivo"
        );
    }

    @Test
    @DisplayName("asignarResponsable debe lanzar BusinessException si el responsable tiene rol no autorizado")
    void testAsignarResponsable_UnauthorizedRole_ThrowsException() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario responsableEstudiante = crearUsuarioActivo(responsableId, "student_user", RolUsuario.ESTUDIANTE);
        Usuario solicitante = crearUsuario(solicitanteId, "otro_estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.CLASIFICADA, solicitante);
        AsignarResponsableDTO dto = new AsignarResponsableDTO();
        dto.setResponsableId(responsableId);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(responsableId)).thenReturn(Optional.of(responsableEstudiante));

        // Act & Assert
        assertThrows(BusinessException.class, () ->
            solicitudService.asignarResponsable(solicitudId, dto, actorId),
            "El responsable no tiene un rol autorizado para atender solicitudes"
        );
    }

    // ============= INICIAR ATENCION TESTS =============

    @Test
    @DisplayName("iniciarAtencion debe pasar a EN_ATENCION cuando solicitud está CLASIFICADA y tiene responsable")
    void testIniciarAtencion_ValidState_Success() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario responsable = crearUsuarioActivo(responsableId, "consultor", RolUsuario.CONSULTOR);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.CLASIFICADA, solicitante);
        solicitud.setResponsable(responsable);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        // Act
        solicitudService.iniciarAtencion(solicitudId, "Iniciando atención", actorId);

        // Assert
        ArgumentCaptor<Solicitud> solicitudCaptor = ArgumentCaptor.forClass(Solicitud.class);
        verify(solicitudRepository).save(solicitudCaptor.capture());
        assertEquals(EstadoSolicitud.EN_ATENCION, solicitudCaptor.getValue().getEstado());
    }

    @Test
    @DisplayName("iniciarAtencion debe lanzar BusinessException si no está CLASIFICADA")
    void testIniciarAtencion_NotClasificada_ThrowsException() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(BusinessException.class, () ->
            solicitudService.iniciarAtencion(solicitudId, "Observación", actorId),
            "Solo se pueden atender solicitudes clasificadas"
        );
    }

    @Test
    @DisplayName("iniciarAtencion debe lanzar BusinessException si no tiene responsable asignado")
    void testIniciarAtencion_NoResponsable_ThrowsException() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.CLASIFICADA, solicitante);
        solicitud.setResponsable(null);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(BusinessException.class, () ->
            solicitudService.iniciarAtencion(solicitudId, "Observación", actorId),
            "La solicitud no tiene responsable asignado"
        );
    }

    // ============= MARCAR ATENDIDA TESTS =============

    @Test
    @DisplayName("marcarAtendida debe pasar a ATENDIDA cuando solicitud está EN_ATENCION")
    void testMarcarAtendida_ValidState_Success() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.EN_ATENCION, solicitante);
        MarcarAtendidoDTO dto = new MarcarAtendidoDTO();
        dto.setObservacion("Solicitud resuelta");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        // Act
        solicitudService.marcarAtendida(solicitudId, dto, actorId);

        // Assert
        ArgumentCaptor<Solicitud> solicitudCaptor = ArgumentCaptor.forClass(Solicitud.class);
        verify(solicitudRepository).save(solicitudCaptor.capture());
        assertEquals(EstadoSolicitud.ATENDIDA, solicitudCaptor.getValue().getEstado());
    }

    @Test
    @DisplayName("marcarAtendida debe lanzar BusinessException si no está EN_ATENCION")
    void testMarcarAtendida_NotEnAtencion_ThrowsException() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.CLASIFICADA, solicitante);
        MarcarAtendidoDTO dto = new MarcarAtendidoDTO();
        dto.setObservacion("Observación");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(BusinessException.class, () ->
            solicitudService.marcarAtendida(solicitudId, dto, actorId),
            "Solo se pueden marcar como atendidas las solicitudes en atención"
        );
    }

    // ============= CERRAR SOLICITUD TESTS =============

    @Test
    @DisplayName("cerrar debe cerrar correctamente una solicitud ATENDIDA")
    void testCerrar_ValidState_Success() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.ATENDIDA, solicitante);
        CerrarSolicitudDTO dto = new CerrarSolicitudDTO();
        dto.setObservacionCierre("Solicitud resuelta correctamente");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        ArgumentCaptor<Solicitud> solicitudCaptor = ArgumentCaptor.forClass(Solicitud.class);

        // Act
        solicitudService.cerrar(solicitudId, dto, actorId);

        // Assert
        verify(solicitudRepository).save(solicitudCaptor.capture());
        Solicitud captured = solicitudCaptor.getValue();
        assertEquals(EstadoSolicitud.CERRADA, captured.getEstado());
        assertEquals("Solicitud resuelta correctamente", captured.getObservacionCierre());
        assertNotNull(captured.getFechaCierre());
    }

    @Test
    @DisplayName("cerrar debe asignar fechaCierre")
    void testCerrar_ShouldSetFechaCierre() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.ATENDIDA, solicitante);
        solicitud.setFechaCierre(null);

        CerrarSolicitudDTO dto = new CerrarSolicitudDTO();
        dto.setObservacionCierre("Cerrada");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);

        // Act
        solicitudService.cerrar(solicitudId, dto, actorId);

        // Assert
        verify(solicitudRepository).save(captor.capture());
        assertNotNull(captor.getValue().getFechaCierre());
    }

    @Test
    @DisplayName("cerrar debe guardar observacionCierre")
    void testCerrar_ShouldSetObservacionCierre() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.ATENDIDA, solicitante);

        CerrarSolicitudDTO dto = new CerrarSolicitudDTO();
        String observacion = "Cambio de calificación aprobado";
        dto.setObservacionCierre(observacion);

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        ArgumentCaptor<Solicitud> captor = ArgumentCaptor.forClass(Solicitud.class);

        // Act
        solicitudService.cerrar(solicitudId, dto, actorId);

        // Assert
        verify(solicitudRepository).save(captor.capture());
        assertEquals(observacion, captor.getValue().getObservacionCierre());
    }

    @Test
    @DisplayName("cerrar debe lanzar BusinessException si no está ATENDIDA")
    void testCerrar_NotAtendida_ThrowsException() {
        // Arrange
        Usuario actor = crearUsuario(actorId, "admin", RolUsuario.ADMINISTRATIVO);
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.EN_ATENCION, solicitante);
        CerrarSolicitudDTO dto = new CerrarSolicitudDTO();
        dto.setObservacionCierre("Intentando cerrar");

        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(BusinessException.class, () ->
            solicitudService.cerrar(solicitudId, dto, actorId),
            "Solo se pueden cerrar las solicitudes atendidas"
        );
    }

    // ============= OBTENER POR ID TESTS =============

    @Test
    @DisplayName("obtenerPorId debe permitir a un ESTUDIANTE ver su propia solicitud")
    void testObtenerPorId_EstudianteVerPropriaSolicitud_Success() {
        // Arrange
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);

        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudMapper.toResponse(solicitud)).thenReturn(responseDTO);

        // Act
        SolicitudResponseDTO result = solicitudService.obtenerPorId(solicitudId, solicitanteId, RolUsuario.ESTUDIANTE);

        // Assert
        assertNotNull(result);
        assertEquals(solicitudId, result.getId());
        verify(solicitudRepository).findById(solicitudId);
    }

    @Test
    @DisplayName("obtenerPorId debe lanzar ResourceNotFoundException si ESTUDIANTE intenta ver solicitud ajena")
    void testObtenerPorId_EstudianteVerSolicitudAjena_ThrowsException() {
        // Arrange
        UUID otroEstudianteId = UUID.randomUUID();
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante1", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            solicitudService.obtenerPorId(solicitudId, otroEstudianteId, RolUsuario.ESTUDIANTE),
            "Solicitud no encontrada con ID: " + solicitudId
        );
    }

    // ============= OBTENER MIS SOLICITUDES TESTS =============

    @Test
    @DisplayName("obtenerMisSolicitudes debe filtrar correctamente por estado")
    void testObtenerMisSolicitudes_FilterByEstado() {
        // Arrange
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);

        Solicitud solicitud1 = crearSolicitud(UUID.randomUUID(), EstadoSolicitud.REGISTRADA, solicitante);
        Solicitud solicitud2 = crearSolicitud(UUID.randomUUID(), EstadoSolicitud.CLASIFICADA, solicitante);

        when(solicitudRepository.findBySolicitanteId(solicitanteId))
            .thenReturn(List.of(solicitud1, solicitud2));
        when(solicitudMapper.toResponse(any())).thenReturn(new SolicitudResponseDTO());

        FiltroSolicitudesDTO filtro = new FiltroSolicitudesDTO();
        filtro.setEstado(EstadoSolicitud.REGISTRADA);

        // Act
        List<SolicitudResponseDTO> result = solicitudService.obtenerMisSolicitudes(solicitanteId, filtro);

        // Assert
        assertEquals(1, result.size());
    }

    // ============= OBTENER MI SOLICITUD TESTS =============

    @Test
    @DisplayName("obtenerMiSolicitud debe retornar solicitud cuando el usuario es el propietario")
    void testObtenerMiSolicitud_OwnerAccess_Success() {
        // Arrange
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);
        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(solicitudMapper.toResponse(solicitud)).thenReturn(responseDTO);

        // Act
        SolicitudResponseDTO result = solicitudService.obtenerMiSolicitud(solicitudId, solicitanteId);

        // Assert
        assertNotNull(result);
        assertEquals(solicitudId, result.getId());
    }

    @Test
    @DisplayName("obtenerMiSolicitud debe lanzar ResourceNotFoundException si el usuario no es el propietario")
    void testObtenerMiSolicitud_NotOwner_ThrowsException() {
        // Arrange
        UUID otherUsuarioId = UUID.randomUUID();
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            solicitudService.obtenerMiSolicitud(solicitudId, otherUsuarioId)
        );

        assertEquals("Solicitud no encontrada o no pertenece al usuario autenticado", exception.getMessage());
    }

    // ============= CONSULTAR SOLICITUDES TESTS =============

    @Test
    @DisplayName("consultar debe retornar lista de solicitudes con filtros aplicados")
    void testConsultar_WithFilters_Success() {
        // Arrange
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.CLASIFICADA, solicitante);

        FiltroSolicitudesDTO filtro = new FiltroSolicitudesDTO();
        filtro.setEstado(EstadoSolicitud.CLASIFICADA);

        when(solicitudRepository.findAll(any(Specification.class))).thenReturn(List.of(solicitud));
        when(solicitudMapper.toResponse(solicitud)).thenReturn(new SolicitudResponseDTO());

        // Act
        List<SolicitudResponseDTO> result = solicitudService.consultar(filtro);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(solicitudRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("consultar debe retornar lista vacía cuando no hay solicitudes que coincidan con filtros")
    void testConsultar_NoMatches_ReturnsEmptyList() {
        // Arrange
        FiltroSolicitudesDTO filtro = new FiltroSolicitudesDTO();

        when(solicitudRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // Act
        List<SolicitudResponseDTO> result = solicitudService.consultar(filtro);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ============= HISTORIAL TESTS =============

    @Test
    @DisplayName("historial debe retornar historial de solicitud para usuario propietario ESTUDIANTE")
    void testHistorial_EstudianteOwner_Success() {
        // Arrange
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);

        HistorialResponseDTO historialDTO = new HistorialResponseDTO();

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId))
            .thenReturn(Collections.emptyList());

        // Act
        List<HistorialResponseDTO> result = solicitudService.historial(solicitudId, solicitanteId, RolUsuario.ESTUDIANTE);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(historialRepository).findBySolicitudIdOrderByFechaHoraAsc(solicitudId);
    }

    @Test
    @DisplayName("historial debe lanzar excepción si ESTUDIANTE intenta ver historial de solicitud ajena")
    void testHistorial_EstudianteNotOwner_ThrowsException() {
        // Arrange
        UUID otherUsuarioId = UUID.randomUUID();
        Usuario solicitante = crearUsuario(solicitanteId, "estudiante", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            solicitudService.historial(solicitudId, otherUsuarioId, RolUsuario.ESTUDIANTE)
        );
    }

    @Test
    @DisplayName("historial debe permitir acceso a ADMINISTRATIVO sin validar propiedad")
    void testHistorial_Administrativo_NoOwnershipCheck() {
        // Arrange
        UUID administrativoId = UUID.randomUUID();
        UUID otherStudentId = UUID.randomUUID();
        Usuario solicitante = crearUsuario(otherStudentId, "otro_estudiante", RolUsuario.ESTUDIANTE);
        Solicitud solicitud = crearSolicitud(solicitudId, EstadoSolicitud.REGISTRADA, solicitante);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(historialRepository.findBySolicitudIdOrderByFechaHoraAsc(solicitudId))
            .thenReturn(Collections.emptyList());

        // Act
        List<HistorialResponseDTO> result = solicitudService.historial(solicitudId, administrativoId, RolUsuario.ADMINISTRATIVO);

        // Assert
        assertNotNull(result);
        verify(historialRepository).findBySolicitudIdOrderByFechaHoraAsc(solicitudId);
    }

    // ============= HELPER METHODS =============

    private Usuario crearUsuario(UUID id, String username, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setIdentificacion(username + "_id");
        return usuario;
    }

    private Usuario crearUsuarioActivo(UUID id, String username, RolUsuario rol) {
        Usuario usuario = crearUsuario(id, username, rol);
        usuario.setActivo(true);
        return usuario;
    }

    private Solicitud crearSolicitud(UUID id, EstadoSolicitud estado, Usuario solicitante) {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(id);
        solicitud.setEstado(estado);
        solicitud.setSolicitante(solicitante);
        solicitud.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);
        solicitud.setDescripcion("Descripción de prueba");
        solicitud.setCanalOrigen(CanalOrigen.CORREO);
        solicitud.setFechaHoraRegistro(LocalDateTime.now());
        return solicitud;
    }

    private SolicitudCreateDTO crearSolicitudCreateDTO(String descripcion) {
        SolicitudCreateDTO dto = new SolicitudCreateDTO();
        dto.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);
        dto.setDescripcion(descripcion);
        dto.setCanalOrigen(CanalOrigen.CORREO);
        return dto;
    }

    private ClasificarPriorizarDTO crearClasificarPriorizarDTO(Prioridad prioridad, String justificacion) {
        ClasificarPriorizarDTO dto = new ClasificarPriorizarDTO();
        dto.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);
        dto.setPrioridad(prioridad);
        dto.setJustificacionPrioridad(justificacion);
        return dto;
    }
}
