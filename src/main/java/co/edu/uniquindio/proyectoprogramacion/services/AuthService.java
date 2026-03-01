package co.edu.uniquindio.proyectoprogramacion.services;

import co.edu.uniquindio.proyectoprogramacion.dto.LoginRequestDTO;
import co.edu.uniquindio.proyectoprogramacion.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}