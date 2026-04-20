package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.mapper.UsuarioMapper;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl Tests")
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, usuarioMapper);
    }

    @Test
    @DisplayName("listarResponsablesActivos debe retornar lista de responsables activos con roles autorizados")
    void testListarResponsablesActivos_ValidResponsibles_Success() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID coordinadorId = UUID.randomUUID();
        UUID consultorId = UUID.randomUUID();

        Usuario admin = crearUsuario(adminId, "admin@uniquindio.edu.co", RolUsuario.ADMINISTRATIVO, true);
        Usuario coordinador = crearUsuario(coordinadorId, "coord@uniquindio.edu.co", RolUsuario.COORDINADOR, true);
        Usuario consultor = crearUsuario(consultorId, "consultor@uniquindio.edu.co", RolUsuario.CONSULTOR, true);

        List<Usuario> usuariosActivos = List.of(admin, coordinador, consultor);

        UsuarioSimpleDTO adminDTO = crearUsuarioSimpleDTO(adminId, "admin@uniquindio.edu.co");
        UsuarioSimpleDTO coordinadorDTO = crearUsuarioSimpleDTO(coordinadorId, "coord@uniquindio.edu.co");
        UsuarioSimpleDTO consultorDTO = crearUsuarioSimpleDTO(consultorId, "consultor@uniquindio.edu.co");

        when(usuarioRepository.findByActivoTrueAndRolIn(
            List.of(RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR)
        )).thenReturn(usuariosActivos);

        when(usuarioMapper.toUsuarioSimple(admin)).thenReturn(adminDTO);
        when(usuarioMapper.toUsuarioSimple(coordinador)).thenReturn(coordinadorDTO);
        when(usuarioMapper.toUsuarioSimple(consultor)).thenReturn(consultorDTO);

        // Act
        List<UsuarioSimpleDTO> result = usuarioService.listarResponsablesActivos();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(adminDTO));
        assertTrue(result.contains(coordinadorDTO));
        assertTrue(result.contains(consultorDTO));

        verify(usuarioRepository).findByActivoTrueAndRolIn(any());
        verify(usuarioMapper).toUsuarioSimple(admin);
        verify(usuarioMapper).toUsuarioSimple(coordinador);
        verify(usuarioMapper).toUsuarioSimple(consultor);
    }

    @Test
    @DisplayName("listarResponsablesActivos debe retornar lista vacía cuando no hay responsables activos")
    void testListarResponsablesActivos_NoActiveResponsibles_ReturnsEmptyList() {
        // Arrange
        when(usuarioRepository.findByActivoTrueAndRolIn(
            List.of(RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR)
        )).thenReturn(Collections.emptyList());

        // Act
        List<UsuarioSimpleDTO> result = usuarioService.listarResponsablesActivos();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepository).findByActivoTrueAndRolIn(any());
    }

    @Test
    @DisplayName("listarResponsablesActivos debe filtrar solo ADMINISTRATIVO, COORDINADOR y CONSULTOR")
    void testListarResponsablesActivos_ShouldFilterByCorrectRoles() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        Usuario admin = crearUsuario(adminId, "admin@uniquindio.edu.co", RolUsuario.ADMINISTRATIVO, true);
        UsuarioSimpleDTO adminDTO = crearUsuarioSimpleDTO(adminId, "admin@uniquindio.edu.co");

        when(usuarioRepository.findByActivoTrueAndRolIn(
            List.of(RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR)
        )).thenReturn(List.of(admin));

        when(usuarioMapper.toUsuarioSimple(admin)).thenReturn(adminDTO);

        // Act
        usuarioService.listarResponsablesActivos();

        // Assert
        verify(usuarioRepository).findByActivoTrueAndRolIn(
            eq(List.of(RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR))
        );
    }

    @Test
    @DisplayName("listarResponsablesActivos debe excluir usuarios inactivos")
    void testListarResponsablesActivos_ShouldExcludeInactiveUsers() {
        // Arrange
        UUID adminId = UUID.randomUUID();
        UUID inactiveAdminId = UUID.randomUUID();

        Usuario adminActivo = crearUsuario(adminId, "admin@uniquindio.edu.co", RolUsuario.ADMINISTRATIVO, true);
        Usuario adminInactivo = crearUsuario(inactiveAdminId, "inactive@uniquindio.edu.co", RolUsuario.ADMINISTRATIVO, false);

        UsuarioSimpleDTO adminDTO = crearUsuarioSimpleDTO(adminId, "admin@uniquindio.edu.co");

        // Repository solo busca activos
        when(usuarioRepository.findByActivoTrueAndRolIn(
            List.of(RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR)
        )).thenReturn(List.of(adminActivo)); // No incluye inactivo

        when(usuarioMapper.toUsuarioSimple(adminActivo)).thenReturn(adminDTO);

        // Act
        List<UsuarioSimpleDTO> result = usuarioService.listarResponsablesActivos();

        // Assert
        assertEquals(1, result.size());
        assertEquals(adminDTO, result.get(0));
    }

    @Test
    @DisplayName("listarResponsablesActivos debe mapear cada usuario correctamente")
    void testListarResponsablesActivos_ShouldMapEachUserCorrectly() {
        // Arrange
        UUID coordinadorId = UUID.randomUUID();
        Usuario coordinador = crearUsuario(coordinadorId, "coord@uniquindio.edu.co", RolUsuario.COORDINADOR, true);
        UsuarioSimpleDTO coordinadorDTO = crearUsuarioSimpleDTO(coordinadorId, "coord@uniquindio.edu.co");

        when(usuarioRepository.findByActivoTrueAndRolIn(any()))
            .thenReturn(List.of(coordinador));
        when(usuarioMapper.toUsuarioSimple(coordinador))
            .thenReturn(coordinadorDTO);

        // Act
        List<UsuarioSimpleDTO> result = usuarioService.listarResponsablesActivos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(coordinadorDTO, result.get(0));
    }

    @Test
    @DisplayName("listarResponsablesActivos debe retornar lista en orden de repository")
    void testListarResponsablesActivos_ShouldPreserveOrder() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        Usuario usuario1 = crearUsuario(id1, "user1@uniquindio.edu.co", RolUsuario.ADMINISTRATIVO, true);
        Usuario usuario2 = crearUsuario(id2, "user2@uniquindio.edu.co", RolUsuario.COORDINADOR, true);
        Usuario usuario3 = crearUsuario(id3, "user3@uniquindio.edu.co", RolUsuario.CONSULTOR, true);

        UsuarioSimpleDTO dto1 = crearUsuarioSimpleDTO(id1, "user1@uniquindio.edu.co");
        UsuarioSimpleDTO dto2 = crearUsuarioSimpleDTO(id2, "user2@uniquindio.edu.co");
        UsuarioSimpleDTO dto3 = crearUsuarioSimpleDTO(id3, "user3@uniquindio.edu.co");

        when(usuarioRepository.findByActivoTrueAndRolIn(any()))
            .thenReturn(List.of(usuario1, usuario2, usuario3));

        when(usuarioMapper.toUsuarioSimple(usuario1)).thenReturn(dto1);
        when(usuarioMapper.toUsuarioSimple(usuario2)).thenReturn(dto2);
        when(usuarioMapper.toUsuarioSimple(usuario3)).thenReturn(dto3);

        // Act
        List<UsuarioSimpleDTO> result = usuarioService.listarResponsablesActivos();

        // Assert
        assertEquals(3, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
        assertEquals(dto3, result.get(2));
    }

    // ============= HELPER METHODS =============

    private Usuario crearUsuario(UUID id, String username, RolUsuario rol, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        usuario.setIdentificacion("id_" + username);
        usuario.setNombres("Usuario");
        usuario.setApellidos("Test");
        usuario.setEmail(username + "@uniquindio.edu.co");
        usuario.setPasswordHash("hashed");
        usuario.setCreadoEn(java.time.LocalDateTime.now());
        return usuario;
    }

    private UsuarioSimpleDTO crearUsuarioSimpleDTO(UUID id, String username) {
        UsuarioSimpleDTO dto = new UsuarioSimpleDTO();
        dto.setId(id);
        dto.setUsername(username);
        return dto;
    }
}
