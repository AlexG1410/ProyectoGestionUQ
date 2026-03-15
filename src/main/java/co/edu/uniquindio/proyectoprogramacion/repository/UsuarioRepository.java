package co.edu.uniquindio.proyectoprogramacion.repository;

import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enumx.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByIdentificacion(String identificacion);

    List<Usuario> findByActivoTrueAndRolIn(List<RolUsuario> roles);
}