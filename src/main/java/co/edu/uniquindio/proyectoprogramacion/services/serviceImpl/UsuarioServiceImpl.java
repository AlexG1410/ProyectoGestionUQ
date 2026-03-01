package co.edu.uniquindio.proyectoprogramacion.services.serviceImpl;

import co.edu.uniquindio.proyectoprogramacion.dto.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.model.Usuario;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.repositories.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public List<UsuarioSimpleDTO> listarResponsablesActivos() {
        List<Usuario> admins = usuarioRepository.findByRolAndActivoTrue(RolUsuario.ADMINISTRATIVO);
        List<Usuario> coords = usuarioRepository.findByRolAndActivoTrue(RolUsuario.COORDINADOR);

        List<UsuarioSimpleDTO> salida = new ArrayList<>();
        admins.forEach(u -> salida.add(map(u)));
        coords.forEach(u -> salida.add(map(u)));

        return salida;
    }

    private UsuarioSimpleDTO map(Usuario u) {
        return UsuarioSimpleDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombreCompleto(u.getNombreCompleto())
                .rol(u.getRol().name())
                .activo(u.isActivo())
                .build();
    }
}