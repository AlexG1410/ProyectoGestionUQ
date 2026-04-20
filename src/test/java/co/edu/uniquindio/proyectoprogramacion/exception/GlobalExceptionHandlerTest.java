package co.edu.uniquindio.proyectoprogramacion.exception;

import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("handleNotFound debe retornar 404 y estructura correcta para ResourceNotFoundException")
    void testHandleNotFound_ShouldReturn404() {
        // Arrange
        String mensajeError = "Solicitud no encontrada";
        ResourceNotFoundException exception = new ResourceNotFoundException(mensajeError);

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleNotFound(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals(mensajeError, response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("handleNotFound debe incluir mensaje de error original")
    void testHandleNotFound_ShouldIncludeErrorMessage() {
        // Arrange
        String mensajeCustom = "Usuario con ID xyz no encontrado";
        ResourceNotFoundException exception = new ResourceNotFoundException(mensajeCustom);

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleNotFound(exception, request);

        // Assert
        assertEquals(mensajeCustom, response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleUnauthorized debe retornar 403 y estructura correcta para UnauthorizedOperationException")
    void testHandleUnauthorized_ShouldReturn403() {
        // Arrange
        String mensajeError = "No autorizado para realizar esta operación";
        UnauthorizedOperationException exception = new UnauthorizedOperationException(mensajeError);

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleUnauthorized(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals(mensajeError, response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("handleUnauthorized debe incluir mensaje de autorización")
    void testHandleUnauthorized_ShouldIncludeAuthMessage() {
        // Arrange
        String mensajeAuth = "Solo administradores pueden ejecutar esta acción";
        UnauthorizedOperationException exception = new UnauthorizedOperationException(mensajeAuth);

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleUnauthorized(exception, request);

        // Assert
        assertEquals(mensajeAuth, response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleBusiness debe retornar 409 y estructura correcta para BusinessException")
    void testHandleBusiness_ShouldReturn409() {
        // Arrange
        String mensajeError = "El responsable está inactivo";
        BusinessException exception = new BusinessException(mensajeError);

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleBusiness(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals(mensajeError, response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("handleBusiness debe incluir mensaje de conflicto de negocio")
    void testHandleBusiness_ShouldIncludeBusinessMessage() {
        // Arrange
        String mensajeNegocio = "La solicitud ya está cerrada, no se puede modificar";
        BusinessException exception = new BusinessException(mensajeNegocio);

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleBusiness(exception, request);

        // Assert
        assertEquals(mensajeNegocio, response.getBody().getMessage());
    }

    @Test
    @DisplayName("handleValidation debe retornar 400 y estructura correcta para MethodArgumentNotValidException")
    void testHandleValidation_ShouldReturn400() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.FieldError fieldError = mock(org.springframework.validation.FieldError.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(fieldError.getField()).thenReturn("email");
        when(fieldError.getDefaultMessage()).thenReturn("debe ser un correo válido");

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleValidation(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("email"));
    }

    @Test
    @DisplayName("handleValidation debe construir mensaje con nombre de campo y error")
    void testHandleValidation_ShouldFormatFieldError() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.FieldError fieldError = mock(org.springframework.validation.FieldError.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(fieldError.getField()).thenReturn("descripcion");
        when(fieldError.getDefaultMessage()).thenReturn("debe tener entre 10 y 2000 caracteres");

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleValidation(exception, request);

        // Assert
        String message = response.getBody().getMessage();
        assertTrue(message.contains("descripcion:"), "Debe contener el nombre del campo");
        assertTrue(message.contains("debe tener entre 10 y 2000 caracteres"), "Debe contener el mensaje de validación");
    }

    @Test
    @DisplayName("handleValidation debe retornar mensaje genérico cuando no hay field errors")
    void testHandleValidation_ShouldReturnGenericMessageWhenNoFieldErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.emptyList());

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleValidation(exception, request);

        // Assert
        assertEquals("Datos inválidos", response.getBody().getMessage());
    }
    /* test*/
    @Test
    @DisplayName("handleNotFound debe incluir path de request")
    void testHandleNotFound_ShouldIncludeRequestPath() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/solicitudes/12345");
        ResourceNotFoundException exception = new ResourceNotFoundException("No encontrado");

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleNotFound(exception, request);

        // Assert
        assertEquals("/api/solicitudes/12345", response.getBody().getPath());
    }

    @Test
    @DisplayName("handleUnauthorized debe incluir path de request")
    void testHandleUnauthorized_ShouldIncludeRequestPath() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/solicitudes/cerrar");
        UnauthorizedOperationException exception = new UnauthorizedOperationException("Acceso denegado");

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleUnauthorized(exception, request);

        // Assert
        assertEquals("/api/solicitudes/cerrar", response.getBody().getPath());
    }

    @Test
    @DisplayName("handleBusiness debe incluir path de request")
    void testHandleBusiness_ShouldIncludeRequestPath() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/solicitudes/clasificar");
        BusinessException exception = new BusinessException("Violación de regla");

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleBusiness(exception, request);

        // Assert
        assertEquals("/api/solicitudes/clasificar", response.getBody().getPath());
    }

    @Test
    @DisplayName("Todos los handlers deben incluir timestamp no nulo")
    void testAllHandlers_ShouldIncludeTimestamp() {
        // Arrange
        ResourceNotFoundException ex1 = new ResourceNotFoundException("Test");
        UnauthorizedOperationException ex2 = new UnauthorizedOperationException("Test");
        BusinessException ex3 = new BusinessException("Test");

        // Act
        ResponseEntity<ApiErrorDTO> response1 = exceptionHandler.handleNotFound(ex1, request);
        ResponseEntity<ApiErrorDTO> response2 = exceptionHandler.handleUnauthorized(ex2, request);
        ResponseEntity<ApiErrorDTO> response3 = exceptionHandler.handleBusiness(ex3, request);

        // Assert
        assertNotNull(response1.getBody().getTimestamp());
        assertNotNull(response2.getBody().getTimestamp());
        assertNotNull(response3.getBody().getTimestamp());
    }

    @Test
    @DisplayName("handleValidation debe retornar timestamp no nulo")
    void testHandleValidation_ShouldIncludeTimestamp() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.emptyList());

        // Act
        ResponseEntity<ApiErrorDTO> response = exceptionHandler.handleValidation(exception, request);

        // Assert
        assertNotNull(response.getBody().getTimestamp());
    }
}
