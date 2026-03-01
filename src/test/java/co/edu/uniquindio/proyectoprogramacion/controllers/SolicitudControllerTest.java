package co.edu.uniquindio.proyectoprogramacion.controllers;

import co.edu.uniquindio.proyectoprogramacion.dto.SolicitudCreateDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.SolicitudResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.model.enums.CanalOrigen;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import co.edu.uniquindio.proyectoprogramacion.security.SecurityConfig;
import co.edu.uniquindio.proyectoprogramacion.services.SolicitudService;
import co.edu.uniquindio.proyectoprogramacion.services.rules.PrioridadRuleEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SolicitudController.class)
@Import(SecurityConfig.class)
class SolicitudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SolicitudService solicitudService;

    @MockBean
    private PrioridadRuleEngine prioridadRuleEngine;

    // Dependencias de seguridad que podrían ser requeridas por tu contexto real.
    // Si tu SecurityConfig exige más beans, me dices y lo ajustamos.
    @MockBean
    private co.edu.uniquindio.proyectoprogramacion.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "admin1", roles = {"ADMINISTRATIVO"})
    void registrarDebeRetornarCreated() throws Exception {
        SolicitudCreateDTO req = new SolicitudCreateDTO();
        req.setTipoSolicitud(TipoSolicitud.HOMOLOGACION);
        req.setDescripcion("Solicitud de prueba");
        req.setCanalOrigen(CanalOrigen.CORREO);
        req.setIdentificacionSolicitante("123456789");
        req.setImpactoAcademico("ALTO");
        req.setFechaLimite("2026-03-15");

        SolicitudResponseDTO response = SolicitudResponseDTO.builder()
                .id(1L)
                .tipoSolicitud("HOMOLOGACION")
                .descripcion("Solicitud de prueba")
                .canalOrigen("CORREO")
                .fechaHoraRegistro(LocalDateTime.now())
                .identificacionSolicitante("123456789")
                .estado("REGISTRADA")
                .cerrada(false)
                .build();

        when(solicitudService.registrar(ArgumentMatchers.any(SolicitudCreateDTO.class), ArgumentMatchers.anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Solicitud registrada correctamente"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.estado").value("REGISTRADA"));
    }
}