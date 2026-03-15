package co.edu.uniquindio.proyectoprogramacion.mapper;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.AuthMeResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleRefDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioMapper {

    public UsuarioSimpleDTO toUsuarioSimpleDTO(Usuario usuario) {
        if (usuario == null) return null;

        return UsuarioSimpleDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .build();
    }

    public UsuarioSimpleRefDTO toUsuarioSimpleRefDTO(Usuario usuario) {
        if (usuario == null) return null;

        return UsuarioSimpleRefDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .build();
    }

    public AuthMeResponseDTO toAuthMeResponseDTO(Usuario usuario) {
        if (usuario == null) return null;

        return AuthMeResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .identificacion(usuario.getIdentificacion())
                .authenticated(true)
                .roles(List.of(usuario.getRol()))
                .activo(Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}