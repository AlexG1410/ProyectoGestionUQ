package co.edu.uniquindio.proyectoprogramacion.mappers;

import co.edu.uniquindio.proyectoprogramacion.dto.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioSimpleDTO toSimpleDTO(Usuario u) {
        return UsuarioSimpleDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombreCompleto(u.getNombreCompleto())
                .rol(u.getRol().name())
                .activo(u.isActivo())
                .build();
    }
}