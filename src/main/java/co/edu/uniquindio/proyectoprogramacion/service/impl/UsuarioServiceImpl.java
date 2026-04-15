package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.mapper.UsuarioMapper;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public List<UsuarioSimpleDTO> listarResponsablesActivos() {
        return usuarioRepository.findByActivoTrueAndRolIn(
                List.of(RolUsuario.ADMINISTRATIVO, RolUsuario.COORDINADOR, RolUsuario.CONSULTOR)
        ).stream()
                .map(usuarioMapper::toUsuarioSimple)
                .toList();
    }
}
