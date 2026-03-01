package co.edu.uniquindio.proyectoprogramacion.repositories;

import co.edu.uniquindio.proyectoprogramacion.model.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByIdentificacion(String identificacion);
    List<Usuario> findByRolAndActivoTrue(RolUsuario rol);
    boolean existsByUsername(String username);
}