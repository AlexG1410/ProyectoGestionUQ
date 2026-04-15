package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.*;

import java.util.UUID;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO dto);
    AuthMeResponseDTO me(UUID userId);
}