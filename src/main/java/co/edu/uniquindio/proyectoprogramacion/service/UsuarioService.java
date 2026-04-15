package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.dto.usuario.UsuarioSimpleDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioSimpleDTO> listarResponsablesActivos();
}
