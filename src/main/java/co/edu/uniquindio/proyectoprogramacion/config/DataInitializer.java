package co.edu.uniquindio.proyectoprogramacion.config;

import co.edu.uniquindio.proyectoprogramacion.model.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearSiNoExiste(
                "coord1", "1001", "Coordinador Principal",
                "123456", RolUsuario.COORDINADOR, true
        );

        crearSiNoExiste(
                "admin1", "1002", "Administrativo UQ",
                "123456", RolUsuario.ADMINISTRATIVO, true
        );

        crearSiNoExiste(
                "est1", "1003", "Estudiante Demo",
                "123456", RolUsuario.ESTUDIANTE, true
        );

        crearSiNoExiste(
                "admin_inactivo", "1004", "Admin Inactivo",
                "123456", RolUsuario.ADMINISTRATIVO, false
        );
    }

    private void crearSiNoExiste(String username, String identificacion, String nombre,
                                 String passwordPlano, RolUsuario rol, boolean activo) {

        if (usuarioRepository.existsByUsername(username)) return;

        Usuario u = Usuario.builder()
                .username(username)
                .identificacion(identificacion)
                .nombreCompleto(nombre)
                .password(passwordEncoder.encode(passwordPlano))
                .rol(rol)
                .activo(activo)
                .build();

        usuarioRepository.save(u);
    }
}