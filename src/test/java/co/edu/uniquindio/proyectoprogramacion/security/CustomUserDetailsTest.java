package co.edu.uniquindio.proyectoprogramacion.security;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomUserDetails Tests")
class CustomUserDetailsTest {

    private CustomUserDetails customUserDetails;
    private Usuario usuario;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        usuario = new Usuario();
        usuario.setId(userId);
        usuario.setUsername("student@uniquindio.edu.co");
        usuario.setPasswordHash("hashedPassword123");
        usuario.setActivo(true);
        usuario.setRol(RolUsuario.ESTUDIANTE);

        customUserDetails = new CustomUserDetails(usuario);
    }

    @Test
    @DisplayName("Constructor debe extraer id del usuario correctamente")
    void testConstructor_ExtractsUserId() {
        // Act
        UUID extractedId = customUserDetails.getId();

        // Assert
        assertNotNull(extractedId);
        assertEquals(userId, extractedId);
    }

    @Test
    @DisplayName("Constructor debe extraer username del usuario correctamente")
    void testConstructor_ExtractsUsername() {
        // Act
        String extractedUsername = customUserDetails.getUsername();

        // Assert
        assertNotNull(extractedUsername);
        assertEquals("student@uniquindio.edu.co", extractedUsername);
    }

    @Test
    @DisplayName("Constructor debe extraer password hash del usuario correctamente")
    void testConstructor_ExtractsPassword() {
        // Act
        String extractedPassword = customUserDetails.getPassword();

        // Assert
        assertNotNull(extractedPassword);
        assertEquals("hashedPassword123", extractedPassword);
    }

    @Test
    @DisplayName("Constructor debe crear authorities con el rol del usuario")
    void testConstructor_CreatesAuthoritiesWithUserRole() {
        // Act
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertFalse(authorities.isEmpty());
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ESTUDIANTE")));
    }

    @Test
    @DisplayName("getAuthorities debe retornar authorities correctas para rol ADMINISTRATIVO")
    void testGetAuthorities_AdminRole_ReturnsAdminAuthority() {
        // Arrange
        usuario.setRol(RolUsuario.ADMINISTRATIVO);
        CustomUserDetails adminUserDetails = new CustomUserDetails(usuario);

        // Act
        Collection<? extends GrantedAuthority> authorities = adminUserDetails.getAuthorities();

        // Assert
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMINISTRATIVO")));
    }

    @Test
    @DisplayName("getAuthorities debe retornar authorities correctas para rol COORDINADOR")
    void testGetAuthorities_CoordinatorRole_ReturnsCoordinatorAuthority() {
        // Arrange
        usuario.setRol(RolUsuario.COORDINADOR);
        CustomUserDetails coordinatorUserDetails = new CustomUserDetails(usuario);

        // Act
        Collection<? extends GrantedAuthority> authorities = coordinatorUserDetails.getAuthorities();

        // Assert
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_COORDINADOR")));
    }

    @Test
    @DisplayName("isAccountNonExpired debe retornar siempre true")
    void testIsAccountNonExpired_AlwaysReturnsTrue() {
        // Act
        boolean result = customUserDetails.isAccountNonExpired();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isAccountNonLocked debe retornar true cuando usuario está activo")
    void testIsAccountNonLocked_ActiveUser_ReturnsTrue() {
        // Arrange
        usuario.setActivo(true);
        CustomUserDetails activeUserDetails = new CustomUserDetails(usuario);

        // Act
        boolean result = activeUserDetails.isAccountNonLocked();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isAccountNonLocked debe retornar false cuando usuario está inactivo")
    void testIsAccountNonLocked_InactiveUser_ReturnsFalse() {
        // Arrange
        usuario.setActivo(false);
        CustomUserDetails inactiveUserDetails = new CustomUserDetails(usuario);

        // Act
        boolean result = inactiveUserDetails.isAccountNonLocked();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isCredentialsNonExpired debe retornar siempre true")
    void testIsCredentialsNonExpired_AlwaysReturnsTrue() {
        // Act
        boolean result = customUserDetails.isCredentialsNonExpired();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isEnabled debe retornar true cuando usuario está activo")
    void testIsEnabled_ActiveUser_ReturnsTrue() {
        // Arrange
        usuario.setActivo(true);
        CustomUserDetails activeUserDetails = new CustomUserDetails(usuario);

        // Act
        boolean result = activeUserDetails.isEnabled();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isEnabled debe retornar false cuando usuario está inactivo")
    void testIsEnabled_InactiveUser_ReturnsFalse() {
        // Arrange
        usuario.setActivo(false);
        CustomUserDetails inactiveUserDetails = new CustomUserDetails(usuario);

        // Act
        boolean result = inactiveUserDetails.isEnabled();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("getId debe retornar el ID del usuario correctamente")
    void testGetId_ReturnsCorrectUserId() {
        // Act
        UUID id = customUserDetails.getId();

        // Assert
        assertNotNull(id);
        assertEquals(userId, id);
    }

    @Test
    @DisplayName("CustomUserDetails debe funcionar correctamente con múltiples roles CONSULTOR")
    void testCustomUserDetails_ConsultorRole_ReturnsConsultorAuthority() {
        // Arrange
        usuario.setRol(RolUsuario.CONSULTOR);
        CustomUserDetails consultorUserDetails = new CustomUserDetails(usuario);

        // Act
        Collection<? extends GrantedAuthority> authorities = consultorUserDetails.getAuthorities();

        // Assert
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CONSULTOR")));
    }
}
