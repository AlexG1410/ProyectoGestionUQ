package co.edu.uniquindio.proyectoprogramacion.model;

import co.edu.uniquindio.proyectoprogramacion.model.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, unique = true)
    private String identificacion; // cédula / código / etc.

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // encriptada

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @Column(nullable = false)
    private boolean activo;
}