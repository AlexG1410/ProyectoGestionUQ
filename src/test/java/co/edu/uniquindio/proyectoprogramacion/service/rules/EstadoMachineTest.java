package co.edu.uniquindio.proyectoprogramacion.service.rules;

import co.edu.uniquindio.proyectoprogramacion.exception.BusinessException;
import co.edu.uniquindio.proyectoprogramacion.model.entity.TransicionEstado;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.repository.TransicionEstadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstadoMachine Tests")
class EstadoMachineTest {

    @Mock
    private TransicionEstadoRepository transicionRepository;

    private EstadoMachine estadoMachine;

    @BeforeEach
    void setUp() {
        estadoMachine = new EstadoMachine(transicionRepository);
    }

    @Test
    @DisplayName("validar debe permitir una transición válida cuando el repositorio la encuentra")
    void testValidar_ValidTransition_ShouldPass() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.CLASIFICADA;

        TransicionEstado transicion = crearTransicion(estadoActual, estadoNuevo, true);

        when(transicionRepository.findByDesdeAndHaciaAndPermitidaTrue(estadoActual, estadoNuevo))
            .thenReturn(Optional.of(transicion));

        // Act & Assert
        assertDoesNotThrow(() -> estadoMachine.validar(estadoActual, estadoNuevo));
    }

    @Test
    @DisplayName("validar debe lanzar BusinessException cuando la transición no está permitida")
    void testValidar_InvalidTransition_ThrowsException() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.REGISTRADA;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.CERRADA; // transición no permitida

        when(transicionRepository.findByDesdeAndHaciaAndPermitidaTrue(estadoActual, estadoNuevo))
            .thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () ->
            estadoMachine.validar(estadoActual, estadoNuevo)
        );

        assertEquals("La transición de estado no es válida", exception.getMessage());
    }

    @Test
    @DisplayName("validar debe lanzar BusinessException cuando se intenta transición a estado bloqueado")
    void testValidar_BlockedTransition_ThrowsException() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.EN_ATENCION;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.REGISTRADA; // ir atrás no es permitido

        when(transicionRepository.findByDesdeAndHaciaAndPermitidaTrue(estadoActual, estadoNuevo))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () ->
            estadoMachine.validar(estadoActual, estadoNuevo)
        );
    }

    @Test
    @DisplayName("validar debe permitir transición CLASIFICADA a EN_ATENCION")
    void testValidar_ClasificadaToEnAtencion_ShouldPass() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CLASIFICADA;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.EN_ATENCION;

        TransicionEstado transicion = crearTransicion(estadoActual, estadoNuevo, true);

        when(transicionRepository.findByDesdeAndHaciaAndPermitidaTrue(estadoActual, estadoNuevo))
            .thenReturn(Optional.of(transicion));

        // Act & Assert
        assertDoesNotThrow(() -> estadoMachine.validar(estadoActual, estadoNuevo));
    }

    @Test
    @DisplayName("validar debe permitir transición EN_ATENCION a ATENDIDA")
    void testValidar_EnAtencionToAtendida_ShouldPass() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.EN_ATENCION;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.ATENDIDA;

        TransicionEstado transicion = crearTransicion(estadoActual, estadoNuevo, true);

        when(transicionRepository.findByDesdeAndHaciaAndPermitidaTrue(estadoActual, estadoNuevo))
            .thenReturn(Optional.of(transicion));

        // Act & Assert
        assertDoesNotThrow(() -> estadoMachine.validar(estadoActual, estadoNuevo));
    }

    @Test
    @DisplayName("validar debe permitir transición ATENDIDA a CERRADA")
    void testValidar_AtendidaToCerrada_ShouldPass() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.ATENDIDA;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.CERRADA;

        TransicionEstado transicion = crearTransicion(estadoActual, estadoNuevo, true);

        when(transicionRepository.findByDesdeAndHaciaAndPermitidaTrue(estadoActual, estadoNuevo))
            .thenReturn(Optional.of(transicion));

        // Act & Assert
        assertDoesNotThrow(() -> estadoMachine.validar(estadoActual, estadoNuevo));
    }

    @Test
    @DisplayName("validar debe lanzar excepción cuando se intenta ir de CERRADA a cualquier estado")
    void testValidar_FromCerrada_ThrowsException() {
        // Arrange
        EstadoSolicitud estadoActual = EstadoSolicitud.CERRADA;
        EstadoSolicitud estadoNuevo = EstadoSolicitud.EN_ATENCION; // intentar reabrir

        when(transicionRepository.findByDesdeAndHaciaAndPermitidaTrue(estadoActual, estadoNuevo))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BusinessException.class, () ->
            estadoMachine.validar(estadoActual, estadoNuevo)
        );
    }

    // ============= HELPER METHODS =============

    private TransicionEstado crearTransicion(
            EstadoSolicitud desde,
            EstadoSolicitud hacia,
            boolean permitida) {
        TransicionEstado transicion = new TransicionEstado();
        transicion.setId(UUID.randomUUID());
        transicion.setDesde(desde);
        transicion.setHacia(hacia);
        transicion.setPermitida(permitida);
        return transicion;
    }
}
