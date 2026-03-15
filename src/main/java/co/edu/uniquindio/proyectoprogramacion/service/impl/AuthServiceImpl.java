package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.*;
import co.edu.uniquindio.proyectoprogramacion.exception.DuplicateResourceException;
import co.edu.uniquindio.proyectoprogramacion.mapper.UsuarioMapper;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.security.CustomUserDetails;
import co.edu.uniquindio.proyectoprogramacion.security.JwtService;
import co.edu.uniquindio.proyectoprogramacion.service.AuthService;
import co.edu.uniquindio.proyectoprogramacion.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecurityUtils securityUtils;
    private final UsuarioMapper usuarioMapper;

    @Override
    public AuthMeResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("El username ya existe");
        }

        if (usuarioRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new DuplicateResourceException("La identificación ya existe");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .identificacion(request.getIdentificacion())
                .rol(request.getRol())
                .activo(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toAuthMeResponseDTO(guardado);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .username(userDetails.getUsername())
                .roles(List.of(userDetails.getUsuario().getRol()))
                .build();
    }

    @Override
    public AuthMeResponseDTO me() {
        Usuario usuario = securityUtils.getUsuarioAutenticado();
        return usuarioMapper.toAuthMeResponseDTO(usuario);
    }
}