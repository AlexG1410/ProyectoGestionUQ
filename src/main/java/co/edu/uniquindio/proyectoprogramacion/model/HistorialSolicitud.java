package co.edu.uniquindio.proyectoprogramacion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_solicitud")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id")
    private SolicitudAcademica solicitud;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 200)
    private String accion; // REGISTRO, CLASIFICACION, PRIORIZACION, ASIGNACION, CAMBIO_ESTADO, CIERRE

    @Column(nullable = false, length = 100)
    private String usuarioResponsable; // username o identificación

    @Column(length = 2000)
    private String observaciones;
}