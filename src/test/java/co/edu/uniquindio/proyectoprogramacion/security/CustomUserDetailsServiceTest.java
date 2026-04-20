package co.edu.uniquindio.proyectoprogramacion.security;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CustomUserDetailsService Tests")
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setUsername("student@uniquindio.edu.co");
        usuario.setPasswordHash("hashedPassword123");
        usuario.setActivo(true);
        usuario.setRol(RolUsuario.CONSULTOR);
    }

    @Test
    @DisplayName("loadUserByUsername debe retornar UserDetails cuando usuario existe")
    void testLoadUserByUsername_UserExists_ReturnsUserDetails() {
        // Arrange
        String username = "student@uniquindio.edu.co";
        when(usuarioRepository.findByUsername(username))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("hashedPassword123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        verify(usuarioRepository).findByUsername(username);
    }

    @Test
    @DisplayName("loadUserByUsername debe lanzar UsernameNotFoundException cuando usuario no existe")
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        String username = "nonexistent@uniquindio.edu.co";
        when(usuarioRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username),
                "Debe lanzar UsernameNotFoundException cuando usuario no existe"
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepository).findByUsername(username);
    }

    @Test
    @DisplayName("loadUserByUsername debe crear CustomUserDetails con autoridades correctas")
    void testLoadUserByUsername_CreatesCustomUserDetailsWithCorrectAuthorities() {
        // Arrange
        String username = "admin@uniquindio.edu.co";
        usuario.setUsername(username);
        usuario.setRol(RolUsuario.ADMINISTRATIVO);
        when(usuarioRepository.findByUsername(username))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMINISTRATIVO")));
    }

    @Test
    @DisplayName("loadUserByUsername debe retornar usuario inactivo como usuario con isEnabled=false")
    void testLoadUserByUsername_InactiveUser_ReturnsDisabledUserDetails() {
        // Arrange
        String username = "inactive@uniquindio.edu.co";
        usuario.setUsername(username);
        usuario.setActivo(false);
        when(usuarioRepository.findByUsername(username))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    @DisplayName("loadUserByUsername debe buscar usuario con el username exacto proporcionado")
    void testLoadUserByUsername_SearchesWithExactUsername() {
        // Arrange
        String username = "coordinator@uniquindio.edu.co";
        when(usuarioRepository.findByUsername(username))
                .thenReturn(Optional.of(usuario));

        // Act
        customUserDetailsService.loadUserByUsername(username);

        // Assert
        verify(usuarioRepository).findByUsername(username);
    }

    @Test
    @DisplayName("loadUserByUsername debe retornar usuario de rol COORDINADOR correctamente")
    void testLoadUserByUsername_CoordinatorRole_ReturnsCorrectRole() {
        // Arrange
        String username = "coordinator@uniquindio.edu.co";
        usuario.setUsername(username);
        usuario.setRol(RolUsuario.COORDINADOR);
        when(usuarioRepository.findByUsername(username))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_COORDINADOR")));
    }

    @Test
    @DisplayName("loadUserByUsername debe retornar usuario de rol CONSULTOR correctamente")
    void testLoadUserByUsername_ConsultorRole_ReturnsCorrectRole() {
        // Arrange
        String username = "consultor@uniquindio.edu.co";
        usuario.setUsername(username);
        usuario.setRol(RolUsuario.CONSULTOR);
        when(usuarioRepository.findByUsername(username))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CONSULTOR")));
    }

    @Test
    @DisplayName("loadUserByUsername debe mantener el estado del usuario intacto")
    void testLoadUserByUsername_PreservesUserState() {
        // Arrange
        UUID userId = UUID.randomUUID();
        usuario.setId(userId);
        usuario.setUsername("test@uniquindio.edu.co");
        when(usuarioRepository.findByUsername("test@uniquindio.edu.co"))
                .thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@uniquindio.edu.co");

        // Assert
        if (userDetails instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
            assertEquals(userId, customUserDetails.getId());
            assertEquals("test@uniquindio.edu.co", customUserDetails.getUsername());
            assertEquals("hashedPassword123", customUserDetails.getPassword());
        }
    }
}
