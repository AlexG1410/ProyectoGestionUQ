package co.edu.uniquindio.proyectoprogramacion.security;

import co.edu.uniquindio.proyectoprogramacion.model.Usuario;
import co.edu.uniquindio.proyectoprogramacion.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!u.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }

        return User.builder()
                .username(u.getUsername())
                .password(u.getPassword()) // ya debe estar encriptada
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())))
                .build();
    }
}