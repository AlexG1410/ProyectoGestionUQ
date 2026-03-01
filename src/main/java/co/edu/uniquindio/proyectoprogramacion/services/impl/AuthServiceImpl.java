package co.edu.uniquindio.proyectoprogramacion.services.impl;

import co.edu.uniquindio.proyectoprogramacion.dto.LoginRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.LoginResponseDTO;
import co.edu.uniquindio.proyectoprogramacion.security.JwtService;
import co.edu.uniquindio.proyectoprogramacion.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return LoginResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }
}