package co.edu.uniquindio.proyectoprogramacion.model.entity;

import co.edu.uniquindio.proyectoprogramacion.model.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "transiciones_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransicionEstado {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud desde;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud hacia;

    @Column(nullable = false)
    private boolean permitida;
}
