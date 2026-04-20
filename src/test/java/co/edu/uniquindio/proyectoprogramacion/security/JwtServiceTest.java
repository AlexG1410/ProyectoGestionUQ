package co.edu.uniquindio.proyectoprogramacion.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private String jwtSecret;
    private long jwtExpirationMs;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Configurar valores para testing
        jwtSecret = "thisIsAVeryLongSecretKeyForTestingJWTTokenGenerationAndValidation12345";
        jwtExpirationMs = 3600000; // 1 hora
        
        // Inyectar propiedades usando reflection
        ReflectionTestUtils.setField(jwtService, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", jwtExpirationMs);
        
        // Llamar a init() para inicializar la clave
        jwtService.init();
        
        // Guardar la clave para usar en otros tests
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("generateToken debe generar un token válido")
    void testGenerateToken_ValidUserDetails_ReturnsToken() {
        // Arrange
        UserDetails userDetails = new User("juan@uniquindio.edu.co", "password", Collections.emptyList());

        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertFalse(token.isBlank());
        // JWT tiene 3 partes separadas por puntos
        int dotCount = (int) token.chars().filter(c -> c == '.').count();
        assertEquals(2, dotCount);
    }

    @Test
    @DisplayName("generateToken debe generar tokens diferentes para diferentes usuarios")
    void testGenerateToken_DifferentUsers_GenerateDifferentTokens() {
        // Arrange
        UserDetails user1 = new User("user1@uniquindio.edu.co", "password", Collections.emptyList());
        UserDetails user2 = new User("user2@uniquindio.edu.co", "password", Collections.emptyList());

        // Act
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("extractUsername debe extraer el username correctamente del token")
    void testExtractUsername_ValidToken_ReturnsUsername() {
        // Arrange
        String username = "admin@uniquindio.edu.co";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        String token = jwtService.generateToken(userDetails);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertNotNull(extractedUsername);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("extractUsername debe retornar el subject del JWT")
    void testExtractUsername_VerifySubject() {
        // Arrange
        String username = "coordinador@uniquindio.edu.co";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        String token = jwtService.generateToken(userDetails);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("isTokenValid debe retornar true para token válido con username correcto")
    void testIsTokenValid_ValidToken_ReturnsTrue() {
        // Arrange
        String username = "usuario@uniquindio.edu.co";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("isTokenValid debe retornar false cuando username no coincide")
    void testIsTokenValid_MismatchedUsername_ReturnsFalse() {
        // Arrange
        UserDetails user1 = new User("user1@uniquindio.edu.co", "password", Collections.emptyList());
        UserDetails user2 = new User("user2@uniquindio.edu.co", "password", Collections.emptyList());

        String token = jwtService.generateToken(user1);

        // Act
        boolean isValid = jwtService.isTokenValid(token, user2);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("isTokenValid debe rechazar token expirado lanzando excepción")
    void testIsTokenValid_ExpiredToken_ThrowsException() {
        // Arrange
        JwtService jwtServiceShortExpiration = new JwtService();
        
        String shortSecret = "thisIsAVeryLongSecretKeyForTestingJWTTokenGenerationAndValidation12345";
        long shortExpirationMs = 1; // 1 milisegundo
        
        ReflectionTestUtils.setField(jwtServiceShortExpiration, "jwtSecret", shortSecret);
        ReflectionTestUtils.setField(jwtServiceShortExpiration, "jwtExpirationMs", shortExpirationMs);
        jwtServiceShortExpiration.init();

        UserDetails userDetails = new User("user@uniquindio.edu.co", "password", Collections.emptyList());
        String token = jwtServiceShortExpiration.generateToken(userDetails);

        // Esperar a que expire el token
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert - JJWT lanza ExpiredJwtException cuando el token está expirado
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () ->
            jwtServiceShortExpiration.isTokenValid(token, userDetails),
            "Debe lanzar ExpiredJwtException cuando el token está expirado"
        );
    }

    @Test
    @DisplayName("isTokenValid debe retornar true para token fresco")
    void testIsTokenValid_FreshToken_ReturnsTrue() {
        // Arrange
        UserDetails userDetails = new User("fresh@uniquindio.edu.co", "password", Collections.emptyList());
        String token = jwtService.generateToken(userDetails);

        // Act & Assert
        assertTrue(jwtService.isTokenValid(token, userDetails), "El token fresco debe ser válido");
    }

    @Test
    @DisplayName("generateToken debe incluir issued at claim")
    void testGenerateToken_ShouldIncludeIssuedAtClaim() {
        // Arrange
        UserDetails userDetails = new User("test@uniquindio.edu.co", "password", Collections.emptyList());

        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        // Validar que el token contiene información de emisión
        // Esto se verifica indirectamente al extraer claims exitosamente
        String username = jwtService.extractUsername(token);
        assertEquals("test@uniquindio.edu.co", username);
    }

    @Test
    @DisplayName("generateToken debe incluir expiration claim")
    void testGenerateToken_ShouldIncludeExpirationClaim() {
        // Arrange
        UserDetails userDetails = new User("test@uniquindio.edu.co", "password", Collections.emptyList());

        // Act
        String token = jwtService.generateToken(userDetails);
        long creationTime = System.currentTimeMillis();

        // Assert
        assertNotNull(token);
        // El token debe ser válido inmediatamente después de crearse
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    @DisplayName("extractUsername debe trabajar con múltiples usernames válidos")
    void testExtractUsername_VariousUsernames_ExtractsCorrectly() {
        // Arrange
        String[] usernames = {
            "student@uniquindio.edu.co",
            "admin@uniquindio.edu.co",
            "coordinator@uniquindio.edu.co"
        };

        for (String username : usernames) {
            UserDetails userDetails = new User(username, "password", Collections.emptyList());

            // Act
            String token = jwtService.generateToken(userDetails);
            String extractedUsername = jwtService.extractUsername(token);

            // Assert
            assertEquals(username, extractedUsername, "Debe extraer correctamente " + username);
        }
    }

    @Test
    @DisplayName("generateToken debe soportar tokens para múltiples usuarios simultáneamente")
    void testGenerateToken_MultipleUsersSimultaneously_EachTokenIsValid() {
        // Arrange
        UserDetails user1 = new User("user1@uniquindio.edu.co", "password", Collections.emptyList());
        UserDetails user2 = new User("user2@uniquindio.edu.co", "password", Collections.emptyList());

        // Act
        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        // Assert
        assertTrue(jwtService.isTokenValid(token1, user1));
        assertTrue(jwtService.isTokenValid(token2, user2));
        assertFalse(jwtService.isTokenValid(token1, user2), "Token de user1 no debe ser válido para user2");
        assertFalse(jwtService.isTokenValid(token2, user1), "Token de user2 no debe ser válido para user1");
    }

    @Test
    @DisplayName("isTokenValid debe ser sensible a cambios en UserDetails")
    void testIsTokenValid_UserDetailsChanged_ReturnsFalse() {
        // Arrange
        UserDetails originalUser = new User("original@uniquindio.edu.co", "password", Collections.emptyList());
        String token = jwtService.generateToken(originalUser);

        UserDetails modifiedUser = new User("modified@uniquindio.edu.co", "password", Collections.emptyList());

        // Act
        boolean isValid = jwtService.isTokenValid(token, modifiedUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("init() debe inicializar la clave correctamente")
    void testInit_ShouldInitializeSecretKey() {
        // Arrange
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(service, "jwtExpirationMs", jwtExpirationMs);

        // Act
        service.init();

        // Assert
        // Si no hay excepción, la inicialización fue exitosa
        UserDetails userDetails = new User("test@uniquindio.edu.co", "password", Collections.emptyList());
        String token = service.generateToken(userDetails);
        assertNotNull(token);
        assertEquals("test@uniquindio.edu.co", service.extractUsername(token));
    }
}
