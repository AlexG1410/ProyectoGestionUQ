package co.edu.uniquindio.proyectoprogramacion.config;

import co.edu.uniquindio.proyectoprogramacion.model.entity.ReglaPriorizacion;
import co.edu.uniquindio.proyectoprogramacion.model.entity.TransicionEstado;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.repository.ReglaPriorizacionRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.TransicionEstadoRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DataInitializer Tests")
@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TransicionEstadoRepository transicionEstadoRepository;

    @Mock
    private ReglaPriorizacionRepository reglaPriorizacionRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    private String[] args;

    @BeforeEach
    void setUp() {
        args = new String[]{};
    }

    @Test
    @DisplayName("initUsers debe crear usuario admin1 cuando no existe")
    void testInitUsers_AdminNotExists_CreatesUser() throws Exception {
        // Arrange
        when(usuarioRepository.existsByUsername("admin1")).thenReturn(false);
        when(usuarioRepository.existsByUsername("coord1")).thenReturn(true);
        when(usuarioRepository.existsByUsername("est1")).thenReturn(true);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        CommandLineRunner runner = dataInitializer.initUsers();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(1)).save(captor.capture());

        Usuario savedUser = captor.getValue();
        assertEquals("admin1", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPasswordHash());
        assertEquals("Administrador", savedUser.getNombres());
        assertEquals("1001", savedUser.getIdentificacion());
        assertTrue(savedUser.isActivo());
    }

    @Test
    @DisplayName("initUsers debe NO crear usuario si ya existe")
    void testInitUsers_AdminExists_DoesNotCreateUser() throws Exception {
        // Arrange
        when(usuarioRepository.existsByUsername("admin1")).thenReturn(true);
        when(usuarioRepository.existsByUsername("coord1")).thenReturn(true);
        when(usuarioRepository.existsByUsername("est1")).thenReturn(true);

        CommandLineRunner runner = dataInitializer.initUsers();

        // Act
        runner.run(args);

        // Assert
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any(Usuario.class));
    }

    @Test
    @DisplayName("initUsers debe crear usuario coordinador cuando no existe")
    void testInitUsers_CoordinatorNotExists_CreatesUser() throws Exception {
        // Arrange
        when(usuarioRepository.existsByUsername("admin1")).thenReturn(true);
        when(usuarioRepository.existsByUsername("coord1")).thenReturn(false);
        when(usuarioRepository.existsByUsername("est1")).thenReturn(true);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        CommandLineRunner runner = dataInitializer.initUsers();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(1)).save(captor.capture());

        Usuario savedUser = captor.getValue();
        assertEquals("coord1", savedUser.getUsername());
        assertEquals("Coordinador", savedUser.getNombres());
        assertEquals(RolUsuario.COORDINADOR, savedUser.getRol());
    }

    @Test
    @DisplayName("initUsers debe crear usuario estudiante cuando no existe")
    void testInitUsers_StudentNotExists_CreatesUser() throws Exception {
        // Arrange
        when(usuarioRepository.existsByUsername("admin1")).thenReturn(true);
        when(usuarioRepository.existsByUsername("coord1")).thenReturn(true);
        when(usuarioRepository.existsByUsername("est1")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        CommandLineRunner runner = dataInitializer.initUsers();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(1)).save(captor.capture());

        Usuario savedUser = captor.getValue();
        assertEquals("est1", savedUser.getUsername());
        assertEquals("Estudiante", savedUser.getNombres());
        assertEquals(RolUsuario.ESTUDIANTE, savedUser.getRol());
    }

    @Test
    @DisplayName("initUsers debe crear múltiples usuarios si no existen")
    void testInitUsers_MultipleUsersNotExist_CreatesAll() throws Exception {
        // Arrange
        when(usuarioRepository.existsByUsername("admin1")).thenReturn(false);
        when(usuarioRepository.existsByUsername("coord1")).thenReturn(false);
        when(usuarioRepository.existsByUsername("est1")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        CommandLineRunner runner = dataInitializer.initUsers();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(3)).save(captor.capture());

        var savedUsers = captor.getAllValues();
        assertEquals(3, savedUsers.size());
        assertTrue(savedUsers.stream().anyMatch(u -> u.getUsername().equals("admin1")));
        assertTrue(savedUsers.stream().anyMatch(u -> u.getUsername().equals("coord1")));
        assertTrue(savedUsers.stream().anyMatch(u -> u.getUsername().equals("est1")));
    }

    @Test
    @DisplayName("initUsers debe encriptar contraseñas correctamente")
    void testInitUsers_EncryptsPasswordCorrectly() throws Exception {
        // Arrange
        when(usuarioRepository.existsByUsername("admin1")).thenReturn(false);
        when(usuarioRepository.existsByUsername("coord1")).thenReturn(true);
        when(usuarioRepository.existsByUsername("est1")).thenReturn(true);
        when(passwordEncoder.encode("123456")).thenReturn("hashedPassword123");

        CommandLineRunner runner = dataInitializer.initUsers();

        // Act
        runner.run(args);

        // Assert
        verify(passwordEncoder).encode("123456");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hashedPassword123", captor.getValue().getPasswordHash());
    }

    @Test
    @DisplayName("initTransiciones debe crear transiciones cuando tabla está vacía")
    void testInitTransiciones_EmptyTable_CreatesTransitions() throws Exception {
        // Arrange
        when(transicionEstadoRepository.count()).thenReturn(0L);

        CommandLineRunner runner = dataInitializer.initTransiciones();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<TransicionEstado> captor = ArgumentCaptor.forClass(TransicionEstado.class);
        verify(transicionEstadoRepository, times(5)).save(captor.capture());

        var savedTransiciones = captor.getAllValues();
        assertTrue(savedTransiciones.stream()
                .anyMatch(t -> t.getDesde() == EstadoSolicitud.REGISTRADA 
                            && t.getHacia() == EstadoSolicitud.CLASIFICADA));
    }

    @Test
    @DisplayName("initTransiciones NO debe crear transiciones si ya existen")
    void testInitTransiciones_DataExists_DoesNotCreate() throws Exception {
        // Arrange
        when(transicionEstadoRepository.count()).thenReturn(5L);

        CommandLineRunner runner = dataInitializer.initTransiciones();

        // Act
        runner.run(args);

        // Assert
        verify(transicionEstadoRepository, never()).save(org.mockito.ArgumentMatchers.any(TransicionEstado.class));
    }

    @Test
    @DisplayName("initTransiciones debe crear transición de reclasificación")
    void testInitTransiciones_CreatesReclassificationTransition() throws Exception {
        // Arrange
        when(transicionEstadoRepository.count()).thenReturn(0L);

        CommandLineRunner runner = dataInitializer.initTransiciones();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<TransicionEstado> captor = ArgumentCaptor.forClass(TransicionEstado.class);
        verify(transicionEstadoRepository, times(5)).save(captor.capture());

        var savedTransiciones = captor.getAllValues();
        assertTrue(savedTransiciones.stream()
                .anyMatch(t -> t.getDesde() == EstadoSolicitud.CLASIFICADA 
                            && t.getHacia() == EstadoSolicitud.CLASIFICADA),
                "Debe incluir transición de reclasificación");
    }

    @Test
    @DisplayName("initReglasPriorizacion debe crear reglas cuando tabla está vacía")
    void testInitReglasPriorizacion_EmptyTable_CreatesRules() throws Exception {
        // Arrange
        when(reglaPriorizacionRepository.count()).thenReturn(0L);

        CommandLineRunner runner = dataInitializer.initReglasPriorizacion();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<ReglaPriorizacion> captor = ArgumentCaptor.forClass(ReglaPriorizacion.class);
        verify(reglaPriorizacionRepository, times(5)).save(captor.capture());

        var savedReglas = captor.getAllValues();
        assertEquals(5, savedReglas.size());
    }

    @Test
    @DisplayName("initReglasPriorizacion NO debe crear reglas si ya existen")
    void testInitReglasPriorizacion_DataExists_DoesNotCreate() throws Exception {
        // Arrange
        when(reglaPriorizacionRepository.count()).thenReturn(5L);

        CommandLineRunner runner = dataInitializer.initReglasPriorizacion();

        // Act
        runner.run(args);

        // Assert
        verify(reglaPriorizacionRepository, never()).save(org.mockito.ArgumentMatchers.any(ReglaPriorizacion.class));
    }

    @Test
    @DisplayName("initReglasPriorizacion debe crear regla HOMOLOGACION")
    void testInitReglasPriorizacion_CreatesHomologacionRule() throws Exception {
        // Arrange
        when(reglaPriorizacionRepository.count()).thenReturn(0L);

        CommandLineRunner runner = dataInitializer.initReglasPriorizacion();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<ReglaPriorizacion> captor = ArgumentCaptor.forClass(ReglaPriorizacion.class);
        verify(reglaPriorizacionRepository, times(5)).save(captor.capture());

        var savedReglas = captor.getAllValues();
        assertTrue(savedReglas.stream()
                .anyMatch(r -> r.getTipoSolicitud() == TipoSolicitud.HOMOLOGACION 
                            && r.getImpactoAcademico() == ImpactoAcademico.ALTO
                            && r.getPrioridadResultante() == Prioridad.ALTA),
                "Debe incluir regla HOMOLOGACION con prioridad ALTA");
    }

    @Test
    @DisplayName("initReglasPriorizacion debe crear regla CANCELACION_ASIGNATURAS")
    void testInitReglasPriorizacion_CreatesCancelacionRule() throws Exception {
        // Arrange
        when(reglaPriorizacionRepository.count()).thenReturn(0L);

        CommandLineRunner runner = dataInitializer.initReglasPriorizacion();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<ReglaPriorizacion> captor = ArgumentCaptor.forClass(ReglaPriorizacion.class);
        verify(reglaPriorizacionRepository, times(5)).save(captor.capture());

        var savedReglas = captor.getAllValues();
        assertTrue(savedReglas.stream()
                .anyMatch(r -> r.getTipoSolicitud() == TipoSolicitud.CANCELACION_ASIGNATURAS 
                            && r.getImpactoAcademico() == ImpactoAcademico.CRITICO
                            && r.getPrioridadResultante() == Prioridad.CRITICA),
                "Debe incluir regla CANCELACION con prioridad CRITICA");
    }

    @Test
    @DisplayName("initReglasPriorizacion debe crear regla SOLICITUD_CUPOS")
    void testInitReglasPriorizacion_CreatesCuposRule() throws Exception {
        // Arrange
        when(reglaPriorizacionRepository.count()).thenReturn(0L);

        CommandLineRunner runner = dataInitializer.initReglasPriorizacion();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<ReglaPriorizacion> captor = ArgumentCaptor.forClass(ReglaPriorizacion.class);
        verify(reglaPriorizacionRepository, times(5)).save(captor.capture());

        var savedReglas = captor.getAllValues();
        assertTrue(savedReglas.stream()
                .anyMatch(r -> r.getTipoSolicitud() == TipoSolicitud.SOLICITUD_CUPOS 
                            && r.getPrioridadResultante() == Prioridad.ALTA),
                "Debe incluir regla SOLICITUD_CUPOS");
    }

    @Test
    @DisplayName("initReglasPriorizacion debe establecer activa=true para todas las reglas")
    void testInitReglasPriorizacion_AllRulesActive() throws Exception {
        // Arrange
        when(reglaPriorizacionRepository.count()).thenReturn(0L);

        CommandLineRunner runner = dataInitializer.initReglasPriorizacion();

        // Act
        runner.run(args);

        // Assert
        ArgumentCaptor<ReglaPriorizacion> captor = ArgumentCaptor.forClass(ReglaPriorizacion.class);
        verify(reglaPriorizacionRepository, times(5)).save(captor.capture());

        var savedReglas = captor.getAllValues();
        assertTrue(savedReglas.stream().allMatch(ReglaPriorizacion::isActiva),
                "Todas las reglas deben estar activas");
    }
}
