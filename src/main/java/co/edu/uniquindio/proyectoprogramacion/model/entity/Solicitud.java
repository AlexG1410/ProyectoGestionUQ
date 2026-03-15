package co.edu.uniquindio.proyectoprogramacion.model.entity;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CanalOrigen canalOrigen;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private TipoSolicitud tipoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ImpactoAcademico impactoAcademico;

    private LocalDate fechaLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSolicitud estado;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Prioridad prioridad;

    @Column(length = 500)
    private String justificacionPrioridad;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(nullable = false)
    private Boolean cerrada;

    private LocalDateTime fechaHoraRegistro;
    private LocalDateTime fechaCierre;

    @Column(length = 1000)
    private String observacionCierre;
}