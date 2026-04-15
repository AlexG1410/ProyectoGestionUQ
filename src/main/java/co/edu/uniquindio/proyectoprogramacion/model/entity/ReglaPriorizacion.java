package co.edu.uniquindio.proyectoprogramacion.model.entity;

import co.edu.uniquindio.proyectoprogramacion.model.enums.ImpactoAcademico;
import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reglas_priorizacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaPriorizacion {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private boolean activa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoSolicitud tipoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImpactoAcademico impactoAcademico;

    @Column(nullable = false)
    private Integer diasAntesVence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridadResultante;

    @Column(nullable = false, length = 500)
    private String justificacionPlantilla;
}
