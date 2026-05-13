package co.edu.uniquindio.proyectoprogramacion.service.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.AuthMeResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.auth.LoginRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.auth.LoginResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.auth.RefreshTokenRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.auth.RefreshTokenResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.exception.ResourceNotFoundException;
import co.edu.uniquindio.proyectoprogramacion.mapper.UsuarioMapper;
import co.edu.uniquindio.proyectoprogramacion.model.entity.Usuario;
import co.edu.uniquindio.proyectoprogramacion.repository.UsuarioRepository;
import co.edu.uniquindio.proyectoprogramacion.security.CustomUserDetails;
import co.edu.uniquindio.proyectoprogramacion.security.JwtService;
import co.edu.uniquindio.proyectoprogramacion.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String token = jwtService.generateToken(new CustomUserDetails(usuario));

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .username(usuario.getUsername())
                .roles(List.of("ROLE_" + usuario.getRol().name()))
                .build();
    }

    @Override
    public AuthMeResponseDTO me(UUID userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return usuarioMapper.toAuthMeResponse(usuario);
    }

    @Override
    public RefreshTokenResponseDTO refresh(RefreshTokenRequestDTO dto) {
        String token = dto.getToken();
        
        // Extraer username del token (verificando validez)
        String username = jwtService.extractUsername(token);
        
        // Cargar usuario
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        // Validar token contra UserDetails
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        if (!jwtService.isTokenValid(token, userDetails)) {
            throw new BadCredentialsException("Token inválido o expirado");
        }
        
        // Generar nuevo token
        String newToken = jwtService.generateToken(userDetails);
        
        return RefreshTokenResponseDTO.builder()
                .token(newToken)
                .type("Bearer")
                .expiresIn(jwtExpirationMs / 1000) // Convertir a segundos
                .build();
    }
}
