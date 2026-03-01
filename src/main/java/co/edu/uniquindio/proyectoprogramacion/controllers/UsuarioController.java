package co.edu.uniquindio.proyectoprogramacion.controllers;

import co.edu.uniquindio.proyectoprogramacion.dto.UsuarioSimpleDTO;
import co.edu.uniquindio.proyectoprogramacion.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyRole('ADMINISTRATIVO','COORDINADOR')")
    @GetMapping("/responsables-activos")
    public ResponseEntity<List<UsuarioSimpleDTO>> listarResponsablesActivos() {
        return ResponseEntity.ok(usuarioService.listarResponsablesActivos());
    }
}