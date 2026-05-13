package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.*;
import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.security.CustomUserDetails;
import co.edu.uniquindio.proyectoprogramacion.service.AuthService;
import co.edu.uniquindio.proyectoprogramacion.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Tests")
@SuppressWarnings("removal")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private SecurityUtils securityUtils;

    private UUID usuarioId;
    private String username;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        username = "juan@uniquindio.edu.co";
    }

    // ============= LOGIN TESTS =============

    @Test
    @DisplayName("POST /api/auth/login debe retornar LoginResponseDTO con status 200")
    void testLogin_ValidCredentials_Success() throws Exception {
        // Arrange
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setUsername(username);
        requestDTO.setPassword("password123");

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .type("Bearer")
                .username(username)
                .roles(List.of("ROLE_ESTUDIANTE"))
                .build();

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login exitoso"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.type").value("Bearer"))
                .andExpect(jsonPath("$.data.username").value(username));
    }

    @Test
    @DisplayName("POST /api/auth/login debe validar que username es obligatorio")
    void testLogin_MissingUsername_BadRequest() throws Exception {
        // Arrange
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    // ============= ME TESTS =============

    @Test
    @DisplayName("GET /api/auth/me debe retornar datos del usuario autenticado")
    @WithMockUser(username = "juan@uniquindio.edu.co", roles = "ESTUDIANTE")
    void testMe_Authenticated_Success() throws Exception {
        // Arrange
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();
        responseDTO.setId(usuarioId);
        responseDTO.setUsername(username);
        responseDTO.setNombreCompleto("Juan Pérez");
        responseDTO.setEmail(username);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(authService.me(usuarioId)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Datos del usuario obtenidos correctamente"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.username").value(username));
    }

    @Test
    @DisplayName("GET /api/auth/me sin autenticación debe retornar 403")
    void testMe_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/auth/me debe llamar a securityUtils.getUsuarioId()")
    @WithMockUser(username = "admin@uniquindio.edu.co", roles = "ADMINISTRATIVO")
    void testMe_ShouldCallSecurityUtils() throws Exception {
        // Arrange
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();
        responseDTO.setId(usuarioId);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(authService.me(usuarioId)).thenReturn(responseDTO);

        // Act
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk());

        // Assert
        verify(securityUtils).getUsuarioId();
        verify(authService).me(usuarioId);
    }

    // ============= REFRESH TOKEN TESTS =============

    @Test
    @DisplayName("POST /api/auth/refresh debe retornar nuevo token con status 200")
    void testRefresh_ValidToken_Success() throws Exception {
        // Arrange
        RefreshTokenRequestDTO requestDTO = RefreshTokenRequestDTO.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.oldtoken...")
                .build();

        RefreshTokenResponseDTO responseDTO = RefreshTokenResponseDTO.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.newtoken...")
                .type("Bearer")
                .expiresIn(86400L)
                .build();

        when(authService.refresh(any(RefreshTokenRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refrescado exitosamente"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.type").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(86400));
    }

    @Test
    @DisplayName("POST /api/auth/refresh debe validar que token es obligatorio")
    void testRefresh_MissingToken_BadRequest() throws Exception {
        // Arrange
        RefreshTokenRequestDTO requestDTO = new RefreshTokenRequestDTO();
        requestDTO.setToken("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/refresh debe ser público (sin requerer autenticación)")
    void testRefresh_NoAuthenticationRequired() throws Exception {
        // Arrange
        RefreshTokenRequestDTO requestDTO = RefreshTokenRequestDTO.builder()
                .token("valid.token.here")
                .build();

        RefreshTokenResponseDTO responseDTO = RefreshTokenResponseDTO.builder()
                .token("new.token.here")
                .type("Bearer")
                .expiresIn(86400L)
                .build();

        when(authService.refresh(any(RefreshTokenRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act & Assert - Debe funcionar sin @WithMockUser
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/auth/refresh debe retornar expiresIn en segundos")
    void testRefresh_ExpiresInSeconds() throws Exception {
        // Arrange
        RefreshTokenRequestDTO requestDTO = RefreshTokenRequestDTO.builder()
                .token("old.jwt.token")
                .build();

        RefreshTokenResponseDTO responseDTO = RefreshTokenResponseDTO.builder()
                .token("new.jwt.token")
                .type("Bearer")
                .expiresIn(86400L) // 24 horas en segundos
                .build();

        when(authService.refresh(any(RefreshTokenRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act
        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String jsonResponse = result.getResponse().getContentAsString();
        assertTrue(jsonResponse.contains("\"expiresIn\":86400"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh debe llamar al AuthService.refresh()")
    void testRefresh_ShouldCallAuthService() throws Exception {
        // Arrange
        RefreshTokenRequestDTO requestDTO = RefreshTokenRequestDTO.builder()
                .token("jwt.token")
                .build();

        RefreshTokenResponseDTO responseDTO = RefreshTokenResponseDTO.builder()
                .token("new.token")
                .type("Bearer")
                .expiresIn(86400L)
                .build();

        when(authService.refresh(any(RefreshTokenRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());

        // Assert
        verify(authService, times(1)).refresh(any(RefreshTokenRequestDTO.class));
    }

    // ============= API RESPONSE STRUCTURE TESTS =============

    @Test
    @DisplayName("Todos los endpoints de auth deben retornar ApiResponseDTO con estructura completa")
    @WithMockUser(username = "test@uniquindio.edu.co", roles = "ESTUDIANTE")
    void testApiResponseStructure() throws Exception {
        // Arrange
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();
        responseDTO.setId(usuarioId);
        responseDTO.setUsername(username);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(authService.me(usuarioId)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").exists());
    }
}
