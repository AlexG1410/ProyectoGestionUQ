package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.model.entity.HistorialSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.AccionHistorial;
import co.edu.uniquindio.proyectoprogramacion.repository.HistorialSolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistorialServiceImpl Tests")
class HistorialServiceImplTest {

    @Mock
    private HistorialSolicitudRepository historialRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private HistorialServiceImpl historialService;
    private UUID solicitudId;
    private UUID actorId;

    @BeforeEach
    void setUp() {
        historialService = new HistorialServiceImpl(historialRepository, solicitudRepository, usuarioRepository);
        solicitudId = UUID.randomUUID();
        actorId = UUID.randomUUID();
    }

    // ============= REGISTRAR HISTORIAL TESTS =============

    @Test
    @DisplayName("registrar debe guardar HistorialSolicitud correctamente cuando solicitud y actor existen")
    void testRegistrar_ValidSolicitudAndActor_Success() {
        // Arrange
        Solicitud solicitud = crearSolicitud(solicitudId);
        Usuario actor = crearUsuario(actorId, "admin@uniquindio.edu.co");
        AccionHistorial accion = AccionHistorial.REGISTRO_SOLICITUD;
        String detalle = "Solicitud registrada";
        String observaciones = "observaciones iniciales";

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));

        // Act
        historialService.registrar(solicitudId, actorId, accion, detalle, observaciones);

        // Assert
        ArgumentCaptor<HistorialSolicitud> historialCaptor = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(historialCaptor.capture());

        HistorialSolicitud guardado = historialCaptor.getValue();
        assertNotNull(guardado);
        assertEquals(solicitud, guardado.getSolicitud());
        assertEquals(actor, guardado.getActor());
        assertEquals(accion, guardado.getAccion());
        assertEquals(detalle, guardado.getDetalle());
        assertEquals(observaciones, guardado.getObservaciones());
        assertNotNull(guardado.getFechaHora());
    }

    @Test
    @DisplayName("registrar debe llamar al repositorio con HistorialSolicitud correcto")
    void testRegistrar_ShouldCallRepositorySave() {
        // Arrange
        Solicitud solicitud = crearSolicitud(solicitudId);
        Usuario actor = crearUsuario(actorId, "consultor@uniquindio.edu.co");
        AccionHistorial accion = AccionHistorial.ASIGNACION_RESPONSABLE;

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));

        // Act
        historialService.registrar(solicitudId, actorId, accion, "Responsable asignado", null);

        // Assert
        verify(historialRepository, times(1)).save(any(HistorialSolicitud.class));
    }

    @Test
    @DisplayName("registrar debe establer fechaHora cuando se guarda el historial")
    void testRegistrar_ShouldSetFechaHora() {
        // Arrange
        Solicitud solicitud = crearSolicitud(solicitudId);
        Usuario actor = crearUsuario(actorId, "coordinator@uniquindio.edu.co");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));

        LocalDateTime antesDeRegistro = LocalDateTime.now();

        // Act
        historialService.registrar(solicitudId, actorId, AccionHistorial.INICIO_ATENCION, "Iniciada", null);

        // Assert
        ArgumentCaptor<HistorialSolicitud> captor = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(captor.capture());

        LocalDateTime fechaHora = captor.getValue().getFechaHora();
        assertNotNull(fechaHora);
        assertTrue(fechaHora.isAfter(antesDeRegistro) || fechaHora.isEqual(antesDeRegistro));
    }

    @Test
    @DisplayName("registrar debe lanzar ResourceNotFoundException si la solicitud no existe")
    void testRegistrar_SolicitudNotFound_ThrowsException() {
        // Arrange
        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            historialService.registrar(solicitudId, actorId, AccionHistorial.REGISTRO_SOLICITUD, "detalle", null)
        );

        assertEquals("Solicitud no encontrada", exception.getMessage());
        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrar debe lanzar ResourceNotFoundException si el actor/usuario no existe")
    void testRegistrar_ActorNotFound_ThrowsException() {
        // Arrange
        Solicitud solicitud = crearSolicitud(solicitudId);

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(actorId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            historialService.registrar(solicitudId, actorId, AccionHistorial.PRIORIZACION_SOLICITUD, "detalle", null)
        );

        assertEquals("Usuario actor no encontrado", exception.getMessage());
        verify(historialRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrar debe permitir observaciones null")
    void testRegistrar_NullObservaciones_Allowed() {
        // Arrange
        Solicitud solicitud = crearSolicitud(solicitudId);
        Usuario actor = crearUsuario(actorId, "student@uniquindio.edu.co");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));

        // Act
        historialService.registrar(solicitudId, actorId, AccionHistorial.CIERRE_SOLICITUD, "detalles", null);

        // Assert
        ArgumentCaptor<HistorialSolicitud> captor = ArgumentCaptor.forClass(HistorialSolicitud.class);
        verify(historialRepository).save(captor.capture());
        assertNull(captor.getValue().getObservaciones());
    }

    @Test
    @DisplayName("registrar debe guardar todas las acciones del historial posibles")
    void testRegistrar_AllAcciones_Success() {
        // Arrange
        Solicitud solicitud = crearSolicitud(solicitudId);
        Usuario actor = crearUsuario(actorId, "user@uniquindio.edu.co");

        when(solicitudRepository.findById(solicitudId)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(actorId)).thenReturn(Optional.of(actor));

        AccionHistorial[] acciones = AccionHistorial.values();

        // Act & Assert
        for (AccionHistorial accion : acciones) {
            historialService.registrar(solicitudId, actorId, accion, "detalle de " + accion.name(), "obs");
        }

        // Verificar que save fue llamado el número correcto de veces
        verify(historialRepository, times(acciones.length)).save(any(HistorialSolicitud.class));
    }

    // ============= HELPER METHODS =============

    private Solicitud crearSolicitud(UUID id) {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(id);
        solicitud.setDescripcion("Solicitud de prueba");
        return solicitud;
    }

    private Usuario crearUsuario(UUID id, String username) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setIdentificacion("id_" + username);
        usuario.setNombres("Usuario");
        usuario.setApellidos("Test");
        usuario.setActivo(true);
        return usuario;
    }
}
