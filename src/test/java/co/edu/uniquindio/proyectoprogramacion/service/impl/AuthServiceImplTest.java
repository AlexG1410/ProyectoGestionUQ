package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.AuthMeResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.auth.LoginResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.mapper.UsuarioMapper;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.security.CustomUserDetails;
import co.edu.uniquindio.proyectoprogramacion.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioMapper usuarioMapper;

    private AuthServiceImpl authService;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(authenticationManager, usuarioRepository, jwtService, usuarioMapper);
        usuarioId = UUID.randomUUID();
    }

    // ============= LOGIN TESTS =============

    @Test
    @DisplayName("login debe autenticar, buscar el usuario, generar token y retornar LoginResponseDTO")
    void testLogin_ValidCredentials_Success() {
        // Arrange
        String username = "juan@uniquindio.edu.co";
        String password = "password123";

        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword(password);

        Usuario usuario = crearUsuario(usuarioId, username, RolUsuario.ESTUDIANTE);
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(username, password));
        when(usuarioRepository.findByUsername(username))
            .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any(CustomUserDetails.class)))
            .thenReturn(expectedToken);

        // Act
        LoginResponseDTO result = authService.login(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("Bearer", result.getType());
        assertEquals(username, result.getUsername());
        assertTrue(result.getRoles().contains("ROLE_ESTUDIANTE"));
    }

    @Test
    @DisplayName("login debe llamar a authenticationManager con credenciales correctas")
    void testLogin_ShouldAuthenticateWithCredentials() {
        // Arrange
        String username = "admin@uniquindio.edu.co";
        String password = "admin123";

        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword(password);

        Usuario usuario = crearUsuario(usuarioId, username, RolUsuario.ADMINISTRATIVO);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(username, password));
        when(usuarioRepository.findByUsername(username))
            .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any(CustomUserDetails.class)))
            .thenReturn("token");

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor = 
            ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        // Act
        authService.login(requestDTO);

        // Assert
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();
        assertEquals(username, capturedToken.getPrincipal());
        assertEquals(password, capturedToken.getCredentials());
    }

    @Test
    @DisplayName("login debe generar token con CustomUserDetails del usuario")
    void testLogin_ShouldGenerateTokenWithCustomUserDetails() {
        // Arrange
        String username = "user@uniquindio.edu.co";
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword("pass");

        Usuario usuario = crearUsuario(usuarioId, username, RolUsuario.COORDINADOR);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(username, "pass"));
        when(usuarioRepository.findByUsername(username))
            .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any(CustomUserDetails.class)))
            .thenReturn("jwt-token");

        ArgumentCaptor<CustomUserDetails> detailsCaptor = ArgumentCaptor.forClass(CustomUserDetails.class);

        // Act
        authService.login(requestDTO);

        // Assert
        verify(jwtService).generateToken(detailsCaptor.capture());
        CustomUserDetails capturedDetails = detailsCaptor.getValue();
        assertNotNull(capturedDetails);
    }

    @Test
    @DisplayName("login debe retornar LoginResponseDTO con roles como ROLE_<ROL>")
    void testLogin_ShouldReturnResponseWithFormattedRoles() {
        // Arrange
        String username = "coordinador@uniquindio.edu.co";
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword("password");

        Usuario usuario = crearUsuario(usuarioId, username, RolUsuario.COORDINADOR);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(username, "password"));
        when(usuarioRepository.findByUsername(username))
            .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any(CustomUserDetails.class)))
            .thenReturn("token-jwt");

        // Act
        LoginResponseDTO result = authService.login(requestDTO);

        // Assert
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertEquals("ROLE_COORDINADOR", result.getRoles().getFirst());
    }

    @Test
    @DisplayName("login debe lanzar ResourceNotFoundException si el usuario no existe después de authenticate")
    void testLogin_UserNotFound_ThrowsException() {
        // Arrange
        String username = "noexiste@uniquindio.edu.co";
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(username, "password"));
        when(usuarioRepository.findByUsername(username))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            authService.login(requestDTO)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("login debe retornar LoginResponseDTO completamente funcional con todos los campos")
    void testLogin_ShouldReturnCompleteResponse() {
        // Arrange
        String username = "estudiante@uniquindio.edu.co";
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword("pass123");

        Usuario usuario = crearUsuario(usuarioId, username, RolUsuario.ESTUDIANTE);
        String generatedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken(username, "pass123"));
        when(usuarioRepository.findByUsername(username))
            .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(any(CustomUserDetails.class)))
            .thenReturn(generatedToken);

        // Act
        LoginResponseDTO result = authService.login(requestDTO);

        // Assert
        assertNotNull(result.getToken());
        assertNotNull(result.getType());
        assertNotNull(result.getUsername());
        assertNotNull(result.getRoles());
        assertEquals(generatedToken, result.getToken());
        assertEquals("Bearer", result.getType());
        assertEquals(username, result.getUsername());
        assertEquals(1, result.getRoles().size());
    }

    // ============= ME TESTS =============

    @Test
    @DisplayName("me debe retornar el usuario mapeado correctamente")
    void testMe_ValidUserId_ReturnsMappedUser() {
        // Arrange
        Usuario usuario = crearUsuario(usuarioId, "juan@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        AuthMeResponseDTO expectedResponse = new AuthMeResponseDTO();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("juan@uniquindio.edu.co");

        when(usuarioRepository.findById(usuarioId))
            .thenReturn(Optional.of(usuario));
        when(usuarioMapper.toAuthMeResponse(usuario))
            .thenReturn(expectedResponse);

        // Act
        AuthMeResponseDTO result = authService.me(usuarioId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("juan@uniquindio.edu.co", result.getUsername());
    }

    @Test
    @DisplayName("me debe buscar el usuario en el repositorio")
    void testMe_ShouldQueryRepository() {
        // Arrange
        Usuario usuario = crearUsuario(usuarioId, "admin@uniquindio.edu.co", RolUsuario.ADMINISTRATIVO);
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();

        when(usuarioRepository.findById(usuarioId))
            .thenReturn(Optional.of(usuario));
        when(usuarioMapper.toAuthMeResponse(usuario))
            .thenReturn(responseDTO);

        // Act
        authService.me(usuarioId);

        // Assert
        verify(usuarioRepository).findById(usuarioId);
    }

    @Test
    @DisplayName("me debe mapear el usuario a AuthMeResponseDTO")
    void testMe_ShouldMapUserToResponse() {
        // Arrange
        Usuario usuario = crearUsuario(usuarioId, "coordinador@uniquindio.edu.co", RolUsuario.COORDINADOR);
        AuthMeResponseDTO expectedResponse = new AuthMeResponseDTO();
        expectedResponse.setUsername("coordinador@uniquindio.edu.co");

        when(usuarioRepository.findById(usuarioId))
            .thenReturn(Optional.of(usuario));
        when(usuarioMapper.toAuthMeResponse(usuario))
            .thenReturn(expectedResponse);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

        // Act
        authService.me(usuarioId);

        // Assert
        verify(usuarioMapper).toAuthMeResponse(usuarioCaptor.capture());
        assertEquals(usuario, usuarioCaptor.getValue());
    }

    @Test
    @DisplayName("me debe lanzar ResourceNotFoundException si el usuario no existe")
    void testMe_UserNotFound_ThrowsException() {
        // Arrange
        when(usuarioRepository.findById(usuarioId))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            authService.me(usuarioId)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("me debe retornar AuthMeResponseDTO completo con datos del usuario")
    void testMe_ReturnCompleteResponse() {
        // Arrange
        Usuario usuario = crearUsuario(usuarioId, "user@uniquindio.edu.co", RolUsuario.ESTUDIANTE);
        usuario.setIdentificacion("1234567890");

        AuthMeResponseDTO expectedResponse = new AuthMeResponseDTO();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("user@uniquindio.edu.co");

        when(usuarioRepository.findById(usuarioId))
            .thenReturn(Optional.of(usuario));
        when(usuarioMapper.toAuthMeResponse(usuario))
            .thenReturn(expectedResponse);

        // Act
        AuthMeResponseDTO result = authService.me(usuarioId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("user@uniquindio.edu.co", result.getUsername());
    }

    // ============= HELPER METHODS =============

    private Usuario crearUsuario(UUID id, String username, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setIdentificacion("id_" + username);
        usuario.setNombres("Usuario");
        usuario.setApellidos("Test");
        usuario.setEmail(username);
        usuario.setPasswordHash("hashed_password");
        usuario.setCreadoEn(java.time.LocalDateTime.now());
        return usuario;
    }
}
