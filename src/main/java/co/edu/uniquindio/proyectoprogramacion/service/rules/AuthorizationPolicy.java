package co.edu.uniquindio.proyectoprogramacion.service.rules;

import co.edu.uniquindio.proyectoprogramacion.exception.UnauthorizedOperationException;
import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class AuthorizationPolicy {

    public void requireAny(RolUsuario rol, RolUsuario... permitidos) {
        if (!Set.of(permitidos).contains(rol)) {
            throw new UnauthorizedOperationException("No autorizado para realizar esta operación");
        }
    }
}
