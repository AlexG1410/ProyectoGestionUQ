package co.edu.uniquindio.proyectoprogramacion.util;

import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    public Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}