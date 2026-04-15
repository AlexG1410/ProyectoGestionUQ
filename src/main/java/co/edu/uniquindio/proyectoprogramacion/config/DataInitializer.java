package co.edu.uniquindio.proyectoprogramacion.config;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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
}