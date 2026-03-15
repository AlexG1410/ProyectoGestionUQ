package co.edu.uniquindio.proyectoprogramacion.service;

import co.edu.uniquindio.proyectoprogramacion.dto.auth.*;

public interface AuthService {
    AuthMeResponseDTO register(RegisterRequestDTO request);
    LoginResponseDTO login(LoginRequestDTO request);
    AuthMeResponseDTO me();
}