package co.edu.uniquindio.proyectoprogramacion.services;

import co.edu.uniquindio.proyectoprogramacion.dto.UsuarioSimpleDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioSimpleDTO> listarResponsablesActivos();
}