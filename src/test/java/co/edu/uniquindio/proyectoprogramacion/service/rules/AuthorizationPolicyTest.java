package co.edu.uniquindio.proyectoprogramacion.service.rules;

import co.edu.uniquindio.proyectoprogramacion.exception.UnauthorizedOperationException;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorizationPolicy Tests")
class AuthorizationPolicyTest {

    private AuthorizationPolicy authorizationPolicy;

    @BeforeEach
    void setUp() {
        authorizationPolicy = new AuthorizationPolicy();
    }

    @Test
    @DisplayName("requireAny debe permitir cuando el rol está en la lista de permitidos")
    void testRequireAny_RoleInPermitted_ShouldPass() {
        // Arrange
        RolUsuario rol = RolUsuario.ADMINISTRATIVO;

        // Act & Assert
        assertDoesNotThrow(() ->
            authorizationPolicy.requireAny(rol, RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR)
        );
    }

    @Test
    @DisplayName("requireAny debe permitir cuando hay un solo rol permitido que coincide")
    void testRequireAny_SingleRoleMatch_ShouldPass() {
        // Arrange
        RolUsuario rol = RolUsuario.ESTUDIANTE;

        // Act & Assert
        assertDoesNotThrow(() ->
            authorizationPolicy.requireAny(rol, RolUsuario.ESTUDIANTE)
        );
    }

    @Test
    @DisplayName("requireAny debe lanzar excepción cuando el rol no está en la lista de permitidos")
    void testRequireAny_RoleNotInPermitted_ThrowsException() {
        // Arrange
        RolUsuario rol = RolUsuario.ESTUDIANTE;

        // Act & Assert
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(rol, RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR)
        );

        assertEquals("No autorizado para realizar esta operación", exception.getMessage());
    }

    @Test
    @DisplayName("requireAny debe lanzar excepción para ESTUDIANTE cuando no está en permitidos")
    void testRequireAny_StudentNotPermitted_ThrowsException() {
        // Arrange
        RolUsuario rol = RolUsuario.ESTUDIANTE;

        // Act & Assert
        assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(rol, RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR)
        );
    }

    @Test
    @DisplayName("requireAny debe permitir ADMINISTRATIVO cuando está en permitidos")
    void testRequireAny_AdminPermitted_ShouldPass() {
        // Arrange
        RolUsuario rol = RolUsuario.ADMINISTRATIVO;

        // Act & Assert
        assertDoesNotThrow(() ->
            authorizationPolicy.requireAny(rol, RolUsuario.ADMINISTRATIVO)
        );
    }

    @Test
    @DisplayName("requireAny debe permitir COORDINADOR cuando está en permitidos")
    void testRequireAny_CoordinatorPermitted_ShouldPass() {
        // Arrange
        RolUsuario rol = RolUsuario.COORDINADOR;

        // Act & Assert
        assertDoesNotThrow(() ->
            authorizationPolicy.requireAny(rol, RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR)
        );
    }

    @Test
    @DisplayName("requireAny debe permitir CONSULTOR cuando está en permitidos")
    void testRequireAny_ConsultorPermitted_ShouldPass() {
        // Arrange
        RolUsuario rol = RolUsuario.CONSULTOR;

        // Act & Assert
        assertDoesNotThrow(() ->
            authorizationPolicy.requireAny(rol, RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR)
        );
    }

    @Test
    @DisplayName("requireAny debe lanzar excepción para CONSULTOR cuando no está en permitidos")
    void testRequireAny_ConsultorNotPermitted_ThrowsException() {
        // Arrange
        RolUsuario rol = RolUsuario.CONSULTOR;

        // Act & Assert
        assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(rol, RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR)
        );
    }

    @Test
    @DisplayName("requireAny debe validar multiplos roles permitidos")
    void testRequireAny_MultipleRoles_ValidatesCorrectly() {
        // Arrange
        RolUsuario rol = RolUsuario.COORDINADOR;

        // Act & Assert - Coordinador es válido en esta lista
        assertDoesNotThrow(() ->
            authorizationPolicy.requireAny(rol, 
                RolUsuario.ADMINISTRATIVO,
                RolUsuario.COORDINADOR,
                RolUsuario.CONSULTOR,
                RolUsuario.ESTUDIANTE
            )
        );
    }

    @Test
    @DisplayName("requireAny debe lanzar excepción cuando se valida contra roles no apliquen")
    void testRequireAny_InvalidRole_ThrowsException() {
        // Arrange
        RolUsuario rol = RolUsuario.CONSULTOR;

        // Act & Assert
        assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(rol, RolUsuario.ESTUDIANTE)
        );
    }

    @Test
    @DisplayName("requireAny debe incluir mensaje de error descriptivo")
    void testRequireAny_ErrorMessage_IsDescriptive() {
        // Arrange
        RolUsuario rol = RolUsuario.ESTUDIANTE;

        // Act & Assert
        UnauthorizedOperationException exception = assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(rol, RolUsuario.ADMINISTRATIVO)
        );

        assertNotNull(exception.getMessage());
        assertFalse(exception.getMessage().isEmpty());
        assertEquals("No autorizado para realizar esta operación", exception.getMessage());
    }

    @Test
    @DisplayName("requireAny debe permitir ADMINISTRATIVO sin otros roles en lista")
    void testRequireAny_OnlyAdmin_ValidateCorrectly() {
        // Act & Assert - Solo ADMINISTRATIVO permitido
        assertDoesNotThrow(() ->
            authorizationPolicy.requireAny(RolUsuario.ADMINISTRATIVO, RolUsuario.ADMINISTRATIVO)
        );

        assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(RolUsuario.COORDINADOR, RolUsuario.ADMINISTRATIVO)
        );
    }

    @Test
    @DisplayName("requireAny debe validar lista vacía de permitidos (edge case)")
    void testRequireAny_EmptyPermittedList_AlwaysThrows() {
        // Act & Assert
        assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(RolUsuario.ADMINISTRATIVO)
        );
    }

    @Test
    @DisplayName("requireAny debe rechazar cualquier rol cuando lista permitida es vacía")
    void testRequireAny_AnyRoleWithEmptyList_ThrowsException() {
        // Arrange
        RolUsuario[] permitidos = new RolUsuario[0];

        // Act & Assert
        assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(RolUsuario.ESTUDIANTE, permitidos)
        );

        assertThrows(UnauthorizedOperationException.class, () ->
            authorizationPolicy.requireAny(RolUsuario.ADMINISTRATIVO, permitidos)
        );
    }
}
