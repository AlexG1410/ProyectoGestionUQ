package co.edu.uniquindio.proyectoprogramacion.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("JwtAuthenticationFilter Tests")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;
    private String validUsername;
    private String validToken;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        validUsername = "student@uniquindio.edu.co";
        validToken = "valid.jwt.token";
        userDetails = new User(validUsername, "password", Collections.emptyList());

        // Limpiar SecurityContext antes de cada test
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal debe pasar al siguiente filtro cuando no hay Authorization header")
    void testDoFilterInternal_NoAuthorizationHeader_PassesToNextFilter() throws ServletException, IOException {
        // Arrange
        // No se establece header Authorization

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal debe pasar al siguiente filtro cuando Authorization header no empieza con Bearer")
    void testDoFilterInternal_InvalidAuthorizationFormat_PassesToNextFilter() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Basic invalidformat");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal debe extraer token del header Bearer correctamente")
    void testDoFilterInternal_ValidBearerFormat_ExtractsToken() throws ServletException, IOException {
        // Arrange
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(validUsername);
        when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).extractUsername(token);
    }

    @Test
    @DisplayName("doFilterInternal debe establecer autenticación cuando token es válido")
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(validUsername);
        when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService).loadUserByUsername(validUsername);
        verify(jwtService).isTokenValid(validToken, userDetails);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(userDetails, authentication.getPrincipal());
    }

    @Test
    @DisplayName("doFilterInternal debe NO establecer autenticación cuando token es inválido")
    void testDoFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(validUsername);
        when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService).isTokenValid(validToken, userDetails);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal debe pasar al siguiente filtro cuando username es null")
    void testDoFilterInternal_NullUsername_PassesToNextFilter() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal debe siempre pasar al siguiente filtro al final")
    void testDoFilterInternal_AlwaysCallsNextFilter() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(validUsername);
        when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal debe NOT sobreescribir autenticación existente")
    void testDoFilterInternal_ExistingAuthentication_DoesNotOverwrite() throws ServletException, IOException {
        // Arrange
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UserDetails existingUser = new User("existing@uniquindio.edu.co", "password", Collections.emptyList());
        Authentication existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                existingUser, null, existingUser.getAuthorities()
        );
        context.setAuthentication(existingAuth);
        SecurityContextHolder.setContext(context);

        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(validUsername);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // La autenticación existente no debe ser reemplazada
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(existingUser, currentAuth.getPrincipal());
        verify(userDetailsService, never()).loadUserByUsername(validUsername);
    }

    @Test
    @DisplayName("doFilterInternal debe cargar UserDetails cuando token es válido")
    void testDoFilterInternal_ValidToken_LoadsUserDetails() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(validUsername);
        when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(userDetailsService).loadUserByUsername(validUsername);
    }

    /*
    @Test
    @DisplayName("doFilterInternal debe rechazar token expirado")
    void testDoFilterInternal_ExpiredToken_RejectedByJwtService() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenThrow(new IllegalArgumentException("Token expired"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    */

    @Test
    @DisplayName("doFilterInternal debe establecer WebAuthenticationDetails con la request actual")
    void testDoFilterInternal_ValidToken_SetsWebAuthenticationDetails() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        request.setRemoteAddr("192.168.1.1");
        when(jwtService.extractUsername(validToken)).thenReturn(validUsername);
        when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertNotNull(authentication.getDetails());
    }

    @Test
    @DisplayName("doFilterInternal debe buscar con el username extraído del token")
    void testDoFilterInternal_UsesExtractedUsername_ForUserDetailsLookup() throws ServletException, IOException {
        // Arrange
        String extractedUsername = "admin@uniquindio.edu.co";
        request.addHeader("Authorization", "Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(extractedUsername);
        when(userDetailsService.loadUserByUsername(extractedUsername)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(userDetailsService).loadUserByUsername(extractedUsername);
    }
}
