package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirClasificacionPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.ia.SugerirPrioridadResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Solicitud;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.*;
import co.edu.uniquindio.proyectoprogramacion.repository.SolicitudRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para IAService con estructura REAL del proyecto
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("IAService Integration Tests")
class IAServiceIntegrationTest {

    @Autowired
    private IAService iaService;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioEstudiante;
    private Usuario usuarioAdministrativo;
    private Solicitud solicitud;

    @BeforeEach
    void setUp() {
        // Crear estudiante
        usuarioEstudiante = usuarioRepository.save(
                Usuario.builder()
                        .username("est_" + UUID.randomUUID().toString().substring(0, 8))
                        .nombres("Carlos")
                        .apellidos("García Pérez")
                        .email("carlos@uniquindio.edu.co")
                        .identificacion("1234567890")
                        .passwordHash("hash")
                        .activo(true)
                        .rol(RolUsuario.ESTUDIANTE)
                        .build()
        );

        // Crear administrativo
        usuarioAdministrativo = usuarioRepository.save(
                Usuario.builder()
                        .username("adm_" + UUID.randomUUID().toString().substring(0, 8))
                        .nombres("María")
                        .apellidos("López")
                        .email("maria@uniquindio.edu.co")
                        .identificacion("9876543210")
                        .passwordHash("hash")
                        .activo(true)
                        .rol(RolUsuario.ADMINISTRATIVO)
                        .build()
        );

        // Crear solicitud
        solicitud = solicitudRepository.save(
                Solicitud.builder()
                        .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                        .descripcion("Solicitud de homologación de asignaturas")
                        .canalOrigen(CanalOrigen.CSU)
                        .impactoAcademico(ImpactoAcademico.ALTO)
                        .fechaLimite(LocalDate.now().plusDays(15))
                        .estado(EstadoSolicitud.REGISTRADA)
                        .prioridad(Prioridad.MEDIA)
                        .justificacionPrioridad("Necesario para homologación")
                        .solicitante(usuarioEstudiante)
                        .fechaHoraRegistro(LocalDateTime.now())
                        .build()
        );
    }

    // ============= TESTS DE RESUMEN =============

    @Test
    @DisplayName("Estudiante puede ver resumen de su propia solicitud")
    void testResumenSolicitud_EstudianteVerSuPropia() {
        // Act
        String resumen = iaService.resumirSolicitud(
                solicitud.getId(),
                usuarioEstudiante.getId(),
                RolUsuario.ESTUDIANTE
        );

        // Assert
        assertNotNull(resumen);
        assertFalse(resumen.isEmpty());
    }

    @Test
    @DisplayName("Estudiante NO puede ver resumen de solicitud ajena")
    void testResumenSolicitud_EstudianteNoVerAjena() {
        // Arrange
        Usuario otroEstudiante = usuarioRepository.save(
                Usuario.builder()
                        .username("otro_" + UUID.randomUUID().toString().substring(0, 8))
                        .nombres("Pedro")
                        .apellidos("Gómez")
                        .email("pedro@uniquindio.edu.co")
                        .identificacion("1111111111")
                        .passwordHash("hash")
                        .activo(true)
                        .rol(RolUsuario.ESTUDIANTE)
                        .build()
        );

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                iaService.resumirSolicitud(
                        solicitud.getId(),
                        otroEstudiante.getId(),
                        RolUsuario.ESTUDIANTE
                )
        );
    }

    @Test
    @DisplayName("Administrativo puede ver resumen de cualquier solicitud")
    void testResumenSolicitud_AdministrativoVerCualquier() {
        // Act
        String resumen = iaService.resumirSolicitud(
                solicitud.getId(),
                usuarioAdministrativo.getId(),
                RolUsuario.ADMINISTRATIVO
        );

        // Assert
        assertNotNull(resumen);
    }

    // ============= TESTS DE SUGERIR PRIORIDAD =============

    @Test
    @DisplayName("sugerirPrioridad retorna valor válido")
    void testSugerirPrioridad_RetornaValorValido() {
        // Arrange
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .fechaLimite(LocalDate.now().plusDays(15))
                .build();

        // Act
        SugerirPrioridadResponseDTO respuesta = iaService.sugerirPrioridad(dto);

        // Assert
        assertNotNull(respuesta);
        assertNotNull(respuesta.getPrioridadSugerida());
        assertNotNull(respuesta.getRazones());
        assertTrue(respuesta.getRazones().size() > 0);
    }

    @Test
    @DisplayName("sugerirPrioridad retorna puntaje entre 0 y 100")
    void testSugerirPrioridad_PuntajeEnRango() {
        // Arrange
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA)
                .impactoAcademico(ImpactoAcademico.BAJO)
                .fechaLimite(LocalDate.now().plusDays(30))
                .build();

        // Act
        SugerirPrioridadResponseDTO respuesta = iaService.sugerirPrioridad(dto);

        // Assert
        assertTrue(respuesta.getPuntajeTotal() >= 0 && respuesta.getPuntajeTotal() <= 100);
    }

    @Test
    @DisplayName("Diferentes tipos de solicitud generan sugerencias")
    void testSugerirPrioridad_DiferentesTipos() {
        // Test varios tipos
        testTipoSolicitud(TipoSolicitud.HOMOLOGACION, ImpactoAcademico.ALTO);
        testTipoSolicitud(TipoSolicitud.REGISTRO_ASIGNATURAS, ImpactoAcademico.MEDIO);
        testTipoSolicitud(TipoSolicitud.CANCELACION_ASIGNATURAS, ImpactoAcademico.BAJO);
        testTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA, ImpactoAcademico.CRITICO);
    }

    private void testTipoSolicitud(TipoSolicitud tipo, ImpactoAcademico impacto) {
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(tipo)
                .impactoAcademico(impacto)
                .fechaLimite(LocalDate.now().plusDays(20))
                .build();

        SugerirPrioridadResponseDTO respuesta = iaService.sugerirPrioridad(dto);

        assertNotNull(respuesta);
        assertNotNull(respuesta.getPrioridadSugerida());
    }

    // ============= TESTS DE CLASIFICACIÓN Y PRIORIDAD =============

    @Test
    @DisplayName("sugerirClasificacionYPrioridad retorna datos completos")
    void testSugerirClasificacion_RetornaDatosCompletos() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion("Estudiante desea cambiar de programa académico")
                .canalOrigen(CanalOrigen.CSU)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .fechaLimite(LocalDate.now().plusDays(20))
                .build();

        // Act
        SugerirClasificacionPrioridadResponseDTO respuesta = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(respuesta);
        assertNotNull(respuesta.getTipoSolicitudSugerido());
        assertNotNull(respuesta.getPrioridadSugerida());
        assertTrue(respuesta.getConfianza() >= 0.0 && respuesta.getConfianza() <= 1.0);
    }

    @Test
    @DisplayName("sugerirClasificacion maneja descripciones largas")
    void testSugerirClasificacion_DescripcionLarga() {
        // Arrange
        String descripcionLarga = "El estudiante ha reflejado durante el semestre actual que " +
                "sus intereses académicos han cambiado significativamente. Solicita cambiar de " +
                "Ingeniería de Sistemas a Ingeniería Electrónica. Esto requiere evaluación completa.";

        SugerirClasificacionPrioridadRequestDTO dto = SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion(descripcionLarga)
                .canalOrigen(CanalOrigen.CORREO)
                .impactoAcademico(ImpactoAcademico.CRITICO)
                .fechaLimite(LocalDate.now().plusDays(5))
                .build();

        // Act
        SugerirClasificacionPrioridadResponseDTO respuesta = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(respuesta);
        assertNotNull(respuesta.getTipoSolicitudSugerido());
    }

    @Test
    @DisplayName("sugerirClasificacion maneja descripciones mínimas")
    void testSugerirClasificacion_DescripcionMinima() {
        // Arrange
        SugerirClasificacionPrioridadRequestDTO dto = SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion("Cambio de programa")
                .canalOrigen(CanalOrigen.TELEFONICO)
                .impactoAcademico(ImpactoAcademico.BAJO)
                .fechaLimite(LocalDate.now().plusDays(30))
                .build();

        // Act
        SugerirClasificacionPrioridadResponseDTO respuesta = iaService.sugerirClasificacionYPrioridad(dto);

        // Assert
        assertNotNull(respuesta);
    }

    // ============= TESTS DE VALIDACIÓN RF-11 =============

    @Test
    @DisplayName("RF-11: Sistema funciona sin IA (fallback disponible)")
    void testRF11_SistemaFuncionaSinIA() {
        // Act: Ejecutar los tres métodos principales
        String resumen = iaService.resumirSolicitud(
                solicitud.getId(),
                usuarioEstudiante.getId(),
                RolUsuario.ESTUDIANTE
        );

        SugerirPrioridadRequestDTO dtoP = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.MEDIO)
                .build();

        SugerirPrioridadResponseDTO sugerenciaPrioridad = iaService.sugerirPrioridad(dtoP);

        SugerirClasificacionPrioridadRequestDTO dtoC = SugerirClasificacionPrioridadRequestDTO.builder()
                .descripcion("Test solicitud")
                .canalOrigen(CanalOrigen.CSU)
                .impactoAcademico(ImpactoAcademico.MEDIO)
                .build();

        SugerirClasificacionPrioridadResponseDTO sugerenciaClasificacion = 
                iaService.sugerirClasificacionYPrioridad(dtoC);

        // Assert: Todo debe estar disponible sin IA
        assertNotNull(resumen, "Resumen debe estar disponible sin IA");
        assertNotNull(sugerenciaPrioridad, "Sugerencia de prioridad debe estar disponible sin IA");
        assertNotNull(sugerenciaClasificacion, "Sugerencia de clasificación debe estar disponible sin IA");
    }

    // ============= TESTS DE ROBUSTEZ =============

    @Test
    @DisplayName("Múltiples llamadas al mismo método no interfieren")
    void testRobustez_MultiplesLlamadas() {
        // Arrange
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                .impactoAcademico(ImpactoAcademico.ALTO)
                .build();

        // Act: Ejecutar 5 veces
        for (int i = 0; i < 5; i++) {
            SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);

            // Assert en cada iteración
            assertNotNull(resultado);
            assertNotNull(resultado.getPrioridadSugerida());
        }
    }

    @Test
    @DisplayName("Servicio es thread-safe")
    void testRobustez_ThreadSafety() throws InterruptedException {
        // Arrange
        SugerirPrioridadRequestDTO dto = SugerirPrioridadRequestDTO.builder()
                .tipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA)
                .impactoAcademico(ImpactoAcademico.CRITICO)
                .build();

        final int NUM_THREADS = 10;
        Thread[] threads = new Thread[NUM_THREADS];

        // Act: Crear múltiples threads
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                SugerirPrioridadResponseDTO resultado = iaService.sugerirPrioridad(dto);
                assertNotNull(resultado);
                assertNotNull(resultado.getPrioridadSugerida());
            });
            threads[i].start();
        }

        // Esperar a que terminen
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    @DisplayName("IAService es inyectable")
    void testImplementacion_EsInyectable() {
        assertNotNull(iaService);
        assertNotNull(iaService.getClass());
    }

    @Test
    @DisplayName("IAService es implementación conocida")
    void testImplementacion_EsImplementacionConocida() {
        String nombreClase = iaService.getClass().getSimpleName();
        boolean esValida = nombreClase.equals("IAServiceLLM") || nombreClase.equals("IAServiceNoop");
        assertTrue(esValida, "Debe ser IAServiceLLM o IAServiceNoop, pero fue: " + nombreClase);
    }
}
