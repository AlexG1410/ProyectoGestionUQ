package co.edu.uniquindio.proyectoprogramacion.model.entity;

import co.edu.uniquindio.proyectoprogramacion.model.enums.Prioridad;
import co.edu.uniquindio.proyectoprogramacion.model.enums.TipoSolicitud;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sugerencias_ia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SugerenciaIA {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TipoSolicitud tipoSolicitudSugerido;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Prioridad prioridadSugerida;

    @Column(length = 2000)
    private String resumenSugerido;

    private Double confianza;

    private Integer puntajeTotal;

    @Column(nullable = false)
    private boolean requiereConfirmacionHumana;

    @Column(nullable = false)
    private boolean confirmada;

    @Column(nullable = false)
    private LocalDateTime generadaEn;
}