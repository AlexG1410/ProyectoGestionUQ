package co.edu.uniquindio.proyectoprogramacion.model.entity;

import co.edu.uniquindio.proyectoprogramacion.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import co.edu.uniquindio.proyectoprogramacion.exception.BusinessException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "solicitudes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Solicitud {

    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    /**
     * Tipo de solicitud - OBLIGATORIO en REGISTRO (RF-01).
     * Se especifica al registrar la solicitud.
     * Valores: REGISTRO_ASIGNATURAS, HOMOLOGACION, CANCELACION_ASIGNATURAS, SOLICITUD_CUPOS, CONSULTA_ACADEMICA, OTRO.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private TipoSolicitud tipoSolicitud;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CanalOrigen canalOrigen;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ImpactoAcademico impactoAcademico;

    private LocalDate fechaLimite;

    @Column(nullable = false)
    private LocalDateTime fechaHoraRegistro;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Prioridad prioridad;

    @Column(length = 500)
    private String justificacionPrioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    private LocalDateTime fechaCierre;

    @Column(length = 1000)
    private String observacionCierre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    public boolean estaCerrada() {
        return EstadoSolicitud.CERRADA.equals(this.estado);
    }

    public void validarModificable() {
        if (estaCerrada()) {
            throw new BusinessException("La solicitud está cerrada y no puede modificarse");
        }
    }
}
