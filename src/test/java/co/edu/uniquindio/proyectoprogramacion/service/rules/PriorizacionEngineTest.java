package co.edu.uniquindio.proyectoprogramacion.service.rules;

import co.edu.uniquindio.proyectoprogramacion.model.entity.ReglaPriorizacion;
import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.repository.ReglaPriorizacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PriorizacionEngine Tests")
class PriorizacionEngineTest {

    @Mock
    private ReglaPriorizacionRepository reglaRepository;

    private PriorizacionEngine priorizacionEngine;

    @BeforeEach
    void setUp() {
        priorizacionEngine = new PriorizacionEngine(reglaRepository);
    }

    @Test
    @DisplayName("calcular debe retornar prioridad MEDIA por defecto cuando no existen reglas activas")
    void testCalcular_NoActiveRules_RetornsMediaDefault() {
        // Arrange
        when(reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO
        )).thenReturn(Collections.emptyList());

        // Act
        PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO,
            LocalDate.now().plusDays(10)
        );

        // Assert
        assertEquals(Prioridad.MEDIA, resultado.prioridad());
        assertEquals("Prioridad asignada por regla por defecto", resultado.justificacion());
    }

    @Test
    @DisplayName("calcular debe usar la regla encontrada cuando la fecha límite está dentro del rango de días")
    void testCalcular_DateWithinRange_UseRule() {
        // Arrange
        ReglaPriorizacion regla = crearReglaPriorizacion(
            ImpactoAcademico.ALTO,
            Prioridad.ALTA,
            5, // días antes de vencer
            "Cambio de calificación urgente"
        );

        LocalDate fechaLimiteProxima = LocalDate.now().plusDays(3); // dentro del rango de 5 días

        when(reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO
        )).thenReturn(List.of(regla));

        // Act
        PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO,
            fechaLimiteProxima
        );

        // Assert
        assertEquals(Prioridad.ALTA, resultado.prioridad());
        assertEquals("Cambio de calificación urgente", resultado.justificacion());
    }

    @Test
    @DisplayName("calcular debe retornar MEDIA cuando la fecha límite no cumple la ventana de la regla")
    void testCalcular_DateOutsideRange_ReturnMedia() {
        // Arrange
        ReglaPriorizacion regla = crearReglaPriorizacion(
            ImpactoAcademico.ALTO,
            Prioridad.CRITICA,
            5, // días antes de vencer
            "Cambio crítico"
        );

        LocalDate fechaLimiteAlejada = LocalDate.now().plusDays(20); // fuera del rango de 5 días

        when(reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO
        )).thenReturn(List.of(regla));

        // Act
        PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO,
            fechaLimiteAlejada
        );

        // Assert
        assertEquals(Prioridad.MEDIA, resultado.prioridad());
    }

    @Test
    @DisplayName("calcular debe retornar la justificación de la regla cuando aplique")
    void testCalcular_RuleApplies_ReturnRuleJustificacion() {
        // Arrange
        String justificacionEsperada = "Solicitud de cambio de calificación dentro de término crítico";
        ReglaPriorizacion regla = crearReglaPriorizacion(
            ImpactoAcademico.MEDIO,
            Prioridad.ALTA,
            7,
            justificacionEsperada
        );

        LocalDate fechaLimite = LocalDate.now().plusDays(5);

        when(reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.MEDIO
        )).thenReturn(List.of(regla));

        // Act
        PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.MEDIO,
            fechaLimite
        );

        // Assert
        assertEquals(justificacionEsperada, resultado.justificacion());
    }

    @Test
    @DisplayName("calcular debe manejar fechaLimite null sin fallar")
    void testCalcular_NullFenaLimite_HandleGracefully() {
        // Arrange
        when(reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.BAJO
        )).thenReturn(Collections.emptyList());

        // Act
        PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.BAJO,
            null
        );

        // Assert
        assertNotNull(resultado);
        assertEquals(Prioridad.MEDIA, resultado.prioridad());
    }

    @Test
    @DisplayName("calcular debe retornar MEDIA cuando fechaLimite es null y hay regla pero no se cumple ventana")
    void testCalcular_NullFechaLimiteWithRule_ReturnMediaDefault() {
        // Arrange
        ReglaPriorizacion regla = crearReglaPriorizacion(
            ImpactoAcademico.ALTO,
            Prioridad.CRITICA,
            5,
            "Urgente"
        );

        when(reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO
        )).thenReturn(List.of(regla));

        // Act - cuando fechaLimite es null, la lógica usa Long.MAX_VALUE en el cálculo de días
        // Así que casi nunca cumplirá la condición dias <= diasAntesVence
        PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO,
            null
        );

        // Assert
        assertEquals(Prioridad.MEDIA, resultado.prioridad());
    }

    @Test
    @DisplayName("calcular debe usar la primera regla cuando hay múltiples coincidencias")
    void testCalcular_MultipleRules_UseFirst() {
        // Arrange
        ReglaPriorizacion regla1 = crearReglaPriorizacion(
            ImpactoAcademico.ALTO,
            Prioridad.ALTA,
            5,
            "Primera regla"
        );
        ReglaPriorizacion regla2 = crearReglaPriorizacion(
            ImpactoAcademico.ALTO,
            Prioridad.CRITICA,
            3,
            "Segunda regla"
        );

        LocalDate fechaLimite = LocalDate.now().plusDays(4);

        when(reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO
        )).thenReturn(List.of(regla1, regla2));

        // Act
        PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.ALTO,
            fechaLimite
        );

        // Assert - debe usar la primera regla de la lista
        assertEquals(Prioridad.ALTA, resultado.prioridad());
        assertEquals("Primera regla", resultado.justificacion());
    }

    @Test
    @DisplayName("calcular debe retornar prioridad correcta cuando está en el límite exacto de días")
    void testCalcular_ExactDayLimit_ApplyRule() {
        // Arrange
        ReglaPriorizacion regla = crearReglaPriorizacion(
            ImpactoAcademico.MEDIO,
            Prioridad.ALTA,
            10, // exactamente 10 días
            "En límite"
        );

        LocalDate fechaLimite = LocalDate.now().plusDays(10); // exactamente el límite

        when(reglaRepository.findByActivaTrueAndTipoSolicitudAndImpactoAcademico(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.MEDIO
        )).thenReturn(List.of(regla));

        // Act
        PriorizacionEngine.ResultadoPriorizacion resultado = priorizacionEngine.calcular(
            TipoSolicitud.CONSULTA_ACADEMICA,
            ImpactoAcademico.MEDIO,
            fechaLimite
        );

        // Assert - '<=', así que exactamente en el límite SÍ aplica
        assertEquals(Prioridad.ALTA, resultado.prioridad());
    }

    // ============= HELPER METHODS =============

    private ReglaPriorizacion crearReglaPriorizacion(
            ImpactoAcademico impacto,
            Prioridad prioridad,
            int diasAntesVence,
            String justificacion) {
        ReglaPriorizacion regla = new ReglaPriorizacion();
        regla.setId(UUID.randomUUID());
        regla.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);
        regla.setImpactoAcademico(impacto);
        regla.setPrioridadResultante(prioridad);
        regla.setDiasAntesVence(diasAntesVence);
        regla.setJustificacionPlantilla(justificacion);
        regla.setActiva(true);
        return regla;
    }
}
