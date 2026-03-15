package co.edu.uniquindio.proyectoprogramacion.service.rules;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.TipoSolicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrioridadRuleEngineTest {

    private PrioridadRuleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PrioridadRuleEngine();
    }

    @Test
    void debeSugerirPrioridadAltaOCriticaCuandoImpactoEsAltoYFechaEsCercana() {
        SugerirPrioridadRequestDTO dto = new SugerirPrioridadRequestDTO();
        dto.setTipoSolicitud(TipoSolicitud.HOMOLOGACION);
        dto.setImpactoAcademico("ALTO");
        dto.setFechaLimite("2026-03-02"); // ajusta si hace falta por fecha actual

        SugerirPrioridadResponseDTO res = engine.sugerir(dto);

        assertNotNull(res);
        assertNotNull(res.getPrioridadSugerida());
        assertTrue(
                res.getPrioridadSugerida().equals("ALTA") || res.getPrioridadSugerida().equals("CRITICA")
        );
        assertNotNull(res.getRazones());
        assertFalse(res.getRazones().isEmpty());
    }

    @Test
    void debeSugerirPrioridadBajaOMediaCuandoImpactoEsBajoYNoHayUrgencia() {
        SugerirPrioridadRequestDTO dto = new SugerirPrioridadRequestDTO();
        dto.setTipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA);
        dto.setImpactoAcademico("BAJO");
        dto.setFechaLimite("2026-12-30");

        SugerirPrioridadResponseDTO res = engine.sugerir(dto);

        assertNotNull(res);
        assertTrue(
                res.getPrioridadSugerida().equals("BAJA") || res.getPrioridadSugerida().equals("MEDIA")
        );
    }
}