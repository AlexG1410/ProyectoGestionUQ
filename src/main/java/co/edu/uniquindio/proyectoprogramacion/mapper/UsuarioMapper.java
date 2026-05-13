package co.edu.uniquindio.proyectoprogramacion.mapper;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.AuthMeResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.solicitud.ResponsableResumenDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioMapper {

    public AuthMeResponseDTO toAuthMeResponse(Usuario usuario) {
        return AuthMeResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombres() + " " + usuario.getApellidos())
                .identificacion(usuario.getIdentificacion())
                .email(usuario.getEmail())
                .authenticated(true)
                .roles(List.of(usuario.getRol()))
                .activo(usuario.isActivo())
                .build();
    }

    public UsuarioSimpleDTO toUsuarioSimple(Usuario usuario) {
        return UsuarioSimpleDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombres() + " " + usuario.getApellidos())
                .rol(usuario.getRol())
                .activo(usuario.isActivo())
                .build();
    }

    public ResponsableResumenDTO toResponsableResumen(Usuario usuario) {
        if (usuario == null) return null;
        return ResponsableResumenDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombres() + " " + usuario.getApellidos())
                .build();
    }
}