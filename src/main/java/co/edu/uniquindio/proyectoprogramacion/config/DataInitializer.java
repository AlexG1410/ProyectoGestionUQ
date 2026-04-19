package co.edu.uniquindio.proyectoprogramacion.config;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.entity.TransicionEstado;
import co.edu.uniquindio.proyectoprogramacion.model.entity.ReglaPriorizacion;
import co.edu.uniquindio.proyectoprogramacion.model.enums.*;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.TransicionEstadoRepository;
import co.edu.uniquindio.proyectoprogramacion.repository.ReglaPriorizacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransicionEstadoRepository transicionEstadoRepository;
    private final ReglaPriorizacionRepository reglaPriorizacionRepository;

    @Bean
    CommandLineRunner initUsers() {
        return args -> {
            if (!usuarioRepository.existsByUsername("admin1")) {
                usuarioRepository.save(Usuario.builder()
                        .username("admin1")
                        .passwordHash(passwordEncoder.encode("123456"))
                        .nombres("Administrador")
                        .apellidos("Principal")
                        .identificacion("1001")
                        .email("admin1@uniquindio.edu.co")
                        .rol(RolUsuario.ADMINISTRATIVO)
                        .activo(true)
                        .creadoEn(java.time.LocalDateTime.now())
                        .build());
            }

            if (!usuarioRepository.existsByUsername("coord1")) {
                usuarioRepository.save(Usuario.builder()
                        .username("coord1")
                        .passwordHash(passwordEncoder.encode("123456"))
                        .nombres("Coordinador")
                        .apellidos("Académico")
                        .identificacion("1002")
                        .email("coord1@uniquindio.edu.co")
                        .rol(RolUsuario.COORDINADOR)
                        .activo(true)
                        .creadoEn(java.time.LocalDateTime.now())
                        .build());
            }

            if (!usuarioRepository.existsByUsername("est1")) {
                usuarioRepository.save(Usuario.builder()
                        .username("est1")
                        .passwordHash(passwordEncoder.encode("123456"))
                        .nombres("Estudiante")
                        .apellidos("Demo")
                        .identificacion("1003")
                        .email("est1@uniquindio.edu.co")
                        .rol(RolUsuario.ESTUDIANTE)
                        .activo(true)
                        .creadoEn(java.time.LocalDateTime.now())
                        .build());
            }
        };
    }

    @Bean
    CommandLineRunner initTransiciones() {
        return args -> {
            if (transicionEstadoRepository.count() == 0) {
                // Flujo válido del ciclo de vida
                transicionEstadoRepository.save(TransicionEstado.builder()
                        .desde(EstadoSolicitud.REGISTRADA)
                        .hacia(EstadoSolicitud.CLASIFICADA)
                        .permitida(true)
                        .build());

                transicionEstadoRepository.save(TransicionEstado.builder()
                        .desde(EstadoSolicitud.CLASIFICADA)
                        .hacia(EstadoSolicitud.EN_ATENCION)
                        .permitida(true)
                        .build());

                transicionEstadoRepository.save(TransicionEstado.builder()
                        .desde(EstadoSolicitud.EN_ATENCION)
                        .hacia(EstadoSolicitud.ATENDIDA)
                        .permitida(true)
                        .build());

                transicionEstadoRepository.save(TransicionEstado.builder()
                        .desde(EstadoSolicitud.ATENDIDA)
                        .hacia(EstadoSolicitud.CERRADA)
                        .permitida(true)
                        .build());

                // Transiciones de reclasificación (si es necesario ajustar)
                transicionEstadoRepository.save(TransicionEstado.builder()
                        .desde(EstadoSolicitud.CLASIFICADA)
                        .hacia(EstadoSolicitud.CLASIFICADA)
                        .permitida(true)
                        .build());
            }
        };
    }

    @Bean
    CommandLineRunner initReglasPriorizacion() {
        return args -> {
            if (reglaPriorizacionRepository.count() == 0) {
                // Regla: HOMOLOGACION + ALTO impacto dentro de 15 días = ALTA prioridad
                reglaPriorizacionRepository.save(ReglaPriorizacion.builder()
                        .activa(true)
                        .tipoSolicitud(TipoSolicitud.HOMOLOGACION)
                        .impactoAcademico(ImpactoAcademico.ALTO)
                        .diasAntesVence(15)
                        .prioridadResultante(Prioridad.ALTA)
                        .justificacionPlantilla("Homologación con alto impacto académico próxima a vencer")
                        .build());

                // Regla: CANCELACION_ASIGNATURAS + CRITICO impacto = CRITICA prioridad
                reglaPriorizacionRepository.save(ReglaPriorizacion.builder()
                        .activa(true)
                        .tipoSolicitud(TipoSolicitud.CANCELACION_ASIGNATURAS)
                        .impactoAcademico(ImpactoAcademico.CRITICO)
                        .diasAntesVence(30)
                        .prioridadResultante(Prioridad.CRITICA)
                        .justificacionPlantilla("Cancelación de asignaturas con impacto crítico")
                        .build());

                // Regla: SOLICITUD_CUPOS + ALTO impacto = ALTA prioridad
                reglaPriorizacionRepository.save(ReglaPriorizacion.builder()
                        .activa(true)
                        .tipoSolicitud(TipoSolicitud.SOLICITUD_CUPOS)
                        .impactoAcademico(ImpactoAcademico.ALTO)
                        .diasAntesVence(10)
                        .prioridadResultante(Prioridad.ALTA)
                        .justificacionPlantilla("Solicitud de cupos con alto impacto académico")
                        .build());

                // Regla: REGISTRO_ASIGNATURAS + MEDIO impacto = MEDIA prioridad
                reglaPriorizacionRepository.save(ReglaPriorizacion.builder()
                        .activa(true)
                        .tipoSolicitud(TipoSolicitud.REGISTRO_ASIGNATURAS)
                        .impactoAcademico(ImpactoAcademico.MEDIO)
                        .diasAntesVence(20)
                        .prioridadResultante(Prioridad.MEDIA)
                        .justificacionPlantilla("Registro de asignaturas con impacto académico medio")
                        .build());

                // Regla: CONSULTA_ACADEMICA + BAJO impacto = BAJA prioridad
                reglaPriorizacionRepository.save(ReglaPriorizacion.builder()
                        .activa(true)
                        .tipoSolicitud(TipoSolicitud.CONSULTA_ACADEMICA)
                        .impactoAcademico(ImpactoAcademico.BAJO)
                        .diasAntesVence(30)
                        .prioridadResultante(Prioridad.BAJA)
                        .justificacionPlantilla("Consulta académica con bajo impacto")
                        .build());
            }
        };
    }
}