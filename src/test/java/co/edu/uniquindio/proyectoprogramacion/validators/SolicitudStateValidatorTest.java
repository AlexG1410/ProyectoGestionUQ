package co.edu.uniquindio.proyectoprogramacion.validators;

import co.edu.uniquindio.proyectoprogramacion.exception.BusinessRuleException;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.EstadoSolicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudStateValidatorTest {

    private SolicitudStateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SolicitudStateValidator();
    }

    @Test
    void debePermitirTransicionValidaDeRegistradaAClasificada() {
        assertDoesNotThrow(() ->
                validator.validarTransicion(EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA)
        );
    }

    @Test
    void debeLanzarExcepcionEnTransicionInvalidaDeRegistradaAAtendida() {
        assertThrows(BusinessRuleException.class, () ->
                validator.validarTransicion(EstadoSolicitud.REGISTRADA, EstadoSolicitud.ATENDIDA)
        );
    }

    @Test
    void noDebePermitirCambiosDesdeCerrada() {
        assertThrows(BusinessRuleException.class, () ->
                validator.validarTransicion(EstadoSolicitud.CERRADA, EstadoSolicitud.EN_ATENCION)
        );
    }
}