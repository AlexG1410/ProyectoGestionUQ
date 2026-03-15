package co.edu.uniquindio.proyectoprogramacion.dto.auth;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.RolUsuario;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 5, max = 100, message = "El nombre completo debe tener entre 5 y 100 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "La identificación es obligatoria")
    @Size(min = 5, max = 20, message = "La identificación debe tener entre 5 y 20 caracteres")
    private String identificacion;

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;
}