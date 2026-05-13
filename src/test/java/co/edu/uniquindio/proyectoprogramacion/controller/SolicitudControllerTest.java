package co.edu.uniquindio.proyectoprogramacion.controller;

import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.*;
import co.edu.uniquindio.proyectoprogramacion.dto.common.ApiResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enums.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.service.SolicitudService;
import co.edu.uniquindio.proyectoprogramacion.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SolicitudController Tests")
@SuppressWarnings("removal")
class SolicitudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SolicitudService solicitudService;

    @MockBean
    private SecurityUtils securityUtils;

    private UUID usuarioId;
    private UUID solicitudId;
    private String username;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        solicitudId = UUID.randomUUID();
        username = "juan@uniquindio.edu.co";
    }

    // ============= REGISTRAR SOLICITUD TESTS =============

    @Test
    @DisplayName("POST /api/solicitudes debe registrar nueva solicitud")
    @WithMockUser(username = "juan@uniquindio.edu.co", roles = "ESTUDIANTE")
    void testRegistrar_ValidData_Success() throws Exception {
        // Arrange
        SolicitudCreateDTO requestDTO = new SolicitudCreateDTO();
        requestDTO.setTipoSolicitud(TipoSolicitud.HOMOLOGACION);
        requestDTO.setDescripcion("Necesito revisión de calificación");
        requestDTO.setCanalOrigen(CanalOrigen.valueOf("CORREO"));

        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);
        responseDTO.setEstado(EstadoSolicitud.REGISTRADA);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.registrar(any(SolicitudCreateDTO.class), eq(usuarioId)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.estado").value("REGISTRADA"));
    }

    @Test
    @DisplayName("POST /api/solicitudes sin autenticación debe retornar 403")
    void testRegistrar_Unauthenticated_Unauthorized() throws Exception {
        // Arrange
        SolicitudCreateDTO requestDTO = new SolicitudCreateDTO();
        requestDTO.setTipoSolicitud(TipoSolicitud.HOMOLOGACION);
        requestDTO.setDescripcion("Descripción");
        requestDTO.setCanalOrigen(CanalOrigen.valueOf("CORREO"));

        // Act & Assert
        mockMvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    // ============= MIS SOLICITUDES CON PAGINACIÓN TESTS =============

    @Test
    @DisplayName("GET /api/solicitudes/mis-solicitudes debe retornar Page con solicitudes del usuario")
    @WithMockUser(username = "juan@uniquindio.edu.co", roles = "ESTUDIANTE")
    void testMisSolicitudes_WithPagination_Success() throws Exception {
        // Arrange
        List<SolicitudResponseDTO> solicitudes = new ArrayList<>();
        SolicitudResponseDTO solicitud1 = new SolicitudResponseDTO();
        solicitud1.setId(UUID.randomUUID());
        solicitud1.setEstado(EstadoSolicitud.REGISTRADA);
        solicitudes.add(solicitud1);

        Page<SolicitudResponseDTO> page = new PageImpl<>(solicitudes, PageRequest.of(0, 20), 1);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.obtenerMisSolicitudes(eq(usuarioId), any(FiltroSolicitudesDTO.class), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/mis-solicitudes")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/solicitudes/mis-solicitudes debe aceptar parámetro de estado")
    @WithMockUser(username = "juan@uniquindio.edu.co", roles = "ESTUDIANTE")
    void testMisSolicitudes_WithEstadoFilter_Success() throws Exception {
        // Arrange
        Page<SolicitudResponseDTO> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.obtenerMisSolicitudes(eq(usuarioId), any(FiltroSolicitudesDTO.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/mis-solicitudes")
                .param("page", "0")
                .param("size", "20")
                .param("estado", "REGISTRADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/solicitudes/mis-solicitudes sin autenticación debe retornar 403")
    void testMisSolicitudes_Unauthenticated_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/mis-solicitudes"))
                .andExpect(status().isForbidden());
    }

    // ============= OBTENER SOLICITUD POR ID TESTS =============

    @Test
    @DisplayName("GET /api/solicitudes/{id} debe retornar solicitud específica")
    @WithMockUser(username = "juan@uniquindio.edu.co", roles = "ESTUDIANTE")
    void testObtenerPorId_ValidId_Success() throws Exception {
        // Arrange
        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);
        responseDTO.setEstado(EstadoSolicitud.REGISTRADA);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(securityUtils.getRolUsuario()).thenReturn(RolUsuario.ESTUDIANTE);
        when(solicitudService.obtenerPorId(eq(solicitudId), eq(usuarioId), eq(RolUsuario.ESTUDIANTE)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/{id}", solicitudId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.estado").value("REGISTRADA"));
    }

    @Test
    @DisplayName("GET /api/solicitudes/{id} debe validar que usuario sea coordinador")
    @WithMockUser(username = "coordinador@uniquindio.edu.co", roles = "COORDINADOR")
    void testObtenerPorId_CoordinadorRole_Success() throws Exception {
        // Arrange
        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(securityUtils.getRolUsuario()).thenReturn(RolUsuario.COORDINADOR);
        when(solicitudService.obtenerPorId(eq(solicitudId), eq(usuarioId), eq(RolUsuario.COORDINADOR)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/{id}", solicitudId))
                .andExpect(status().isOk());
    }

    // ============= CLASIFICAR Y PRIORIZAR TESTS =============

    @Test
    @DisplayName("PUT /api/solicitudes/{id}/clasificar debe clasificar solicitud")
    @WithMockUser(username = "admin@uniquindio.edu.co", roles = "ADMINISTRATIVO")
    void testClasificar_ValidData_Success() throws Exception {
        // Arrange
        ClasificarPriorizarDTO requestDTO = new ClasificarPriorizarDTO();
        requestDTO.setTipoSolicitud(TipoSolicitud.HOMOLOGACION);

        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);
        responseDTO.setEstado(EstadoSolicitud.CLASIFICADA);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.clasificarPriorizar(eq(solicitudId), any(ClasificarPriorizarDTO.class), eq(usuarioId)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/solicitudes/{id}/clasificar", solicitudId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("CLASIFICADA"));
    }

    @Test
    @DisplayName("PUT /api/solicitudes/{id}/clasificar solo para ADMINISTRATIVO y COORDINADOR")
    @WithMockUser(username = "juan@uniquindio.edu.co", roles = "ESTUDIANTE")
    void testClasificar_EstudianteRole_Forbidden() throws Exception {
        // Arrange
        ClasificarPriorizarDTO requestDTO = new ClasificarPriorizarDTO();
        requestDTO.setTipoSolicitud(TipoSolicitud.HOMOLOGACION);

        // Act & Assert
        mockMvc.perform(put("/api/solicitudes/{id}/clasificar", solicitudId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    // ============= ASIGNAR RESPONSABLE TESTS =============

    @Test
    @DisplayName("PUT /api/solicitudes/{id}/asignar debe asignar responsable")
    @WithMockUser(username = "admin@uniquindio.edu.co", roles = "ADMINISTRATIVO")
    void testAsignarResponsable_ValidData_Success() throws Exception {
        // Arrange
        UUID responsableId = UUID.randomUUID();
        AsignarResponsableDTO requestDTO = new AsignarResponsableDTO();
        requestDTO.setResponsableId(responsableId);

        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);
        responseDTO.setEstado(EstadoSolicitud.CLASIFICADA);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.asignarResponsable(eq(solicitudId), any(AsignarResponsableDTO.class), eq(usuarioId)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/solicitudes/{id}/asignar", solicitudId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ============= INICIAR ATENCIÓN TESTS =============

    @Test
    @DisplayName("PUT /api/solicitudes/{id}/iniciar-atencion debe iniciar atención")
    @WithMockUser(username = "admin@uniquindio.edu.co", roles = "ADMINISTRATIVO")
    void testIniciarAtencion_ValidData_Success() throws Exception {
        // Arrange
        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);
        responseDTO.setEstado(EstadoSolicitud.EN_ATENCION);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.iniciarAtencion(eq(solicitudId), anyString(), eq(usuarioId)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/solicitudes/{id}/iniciar-atencion", solicitudId)
                .param("observacion", "Iniciando atención"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("EN_ATENCION"));
    }

    // ============= MARCAR ATENDIDA TESTS =============

    @Test
    @DisplayName("PUT /api/solicitudes/{id}/marcar-atendida debe marcar solicitud como atendida")
    @WithMockUser(username = "admin@uniquindio.edu.co", roles = "ADMINISTRATIVO")
    void testMarcarAtendida_ValidData_Success() throws Exception {
        // Arrange
        MarcarAtendidoDTO requestDTO = new MarcarAtendidoDTO();
        requestDTO.setObservacion("Solicitud atendida correctamente");

        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);
        responseDTO.setEstado(EstadoSolicitud.ATENDIDA);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.marcarAtendida(eq(solicitudId), any(MarcarAtendidoDTO.class), eq(usuarioId)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/solicitudes/{id}/marcar-atendida", solicitudId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("ATENDIDA"));
    }

    // ============= CERRAR SOLICITUD TESTS =============

    @Test
    @DisplayName("PUT /api/solicitudes/{id}/cerrar debe cerrar solicitud")
    @WithMockUser(username = "admin@uniquindio.edu.co", roles = "ADMINISTRATIVO")
    void testCerrar_ValidData_Success() throws Exception {
        // Arrange
        CerrarSolicitudDTO requestDTO = new CerrarSolicitudDTO();
        requestDTO.setObservacionCierre("Solicitud resuelta exitosamente");

        SolicitudResponseDTO responseDTO = new SolicitudResponseDTO();
        responseDTO.setId(solicitudId);
        responseDTO.setEstado(EstadoSolicitud.CERRADA);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.cerrar(eq(solicitudId), any(CerrarSolicitudDTO.class), eq(usuarioId)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/solicitudes/{id}/cerrar", solicitudId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estado").value("CERRADA"));
    }

    // ============= AUTORIZACIÓN TESTS =============

    @Test
    @DisplayName("Endpoints de solicitud deben validar autorización por rol")
    @WithMockUser(username = "consultor@uniquindio.edu.co", roles = "CONSULTOR")
    void testSolicitudEndpoints_ConsultorRole() throws Exception {
        // Los consultores solo pueden ver, no modificar
        SolicitudCreateDTO requestDTO = new SolicitudCreateDTO();
        requestDTO.setTipoSolicitud(TipoSolicitud.HOMOLOGACION);
        requestDTO.setDescripcion("Descripción de solicitud válida para testing");
        requestDTO.setCanalOrigen(CanalOrigen.valueOf("CORREO"));
        
        mockMvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    // ============= API RESPONSE STRUCTURE TESTS =============

    @Test
    @DisplayName("GET /api/solicitudes/mis-solicitudes debe retornar estructura ApiResponseDTO completa")
    @WithMockUser(username = "juan@uniquindio.edu.co", roles = "ESTUDIANTE")
    void testApiResponseStructure_Complete() throws Exception {
        // Arrange
        Page<SolicitudResponseDTO> page = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20), 0);

        when(securityUtils.getUsuarioId()).thenReturn(usuarioId);
        when(solicitudService.obtenerMisSolicitudes(eq(usuarioId), any(FiltroSolicitudesDTO.class), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/solicitudes/mis-solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.totalPages").isNumber());
    }
}
