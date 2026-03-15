package co.edu.uniquindio.proyectoprogramacion.config;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.RolUsuario;
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
                        .password(passwordEncoder.encode("123456"))
                        .nombreCompleto("Administrador Principal")
                        .identificacion("1001")
                        .rol(RolUsuario.ADMINISTRATIVO)
                        .activo(true)
                        .build());
            }

            if (!usuarioRepository.existsByUsername("coord1")) {
                usuarioRepository.save(Usuario.builder()
                        .username("coord1")
                        .password(passwordEncoder.encode("123456"))
                        .nombreCompleto("Coordinador Académico")
                        .identificacion("1002")
                        .rol(RolUsuario.COORDINADOR)
                        .activo(true)
                        .build());
            }

            if (!usuarioRepository.existsByUsername("est1")) {
                usuarioRepository.save(Usuario.builder()
                        .username("est1")
                        .password(passwordEncoder.encode("123456"))
                        .nombreCompleto("Estudiante Demo")
                        .identificacion("1003")
                        .rol(RolUsuario.ESTUDIANTE)
                        .activo(true)
                        .build());
            }
        };
    }
}