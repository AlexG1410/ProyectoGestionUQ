package co.edu.uniquindio.proyectoprogramacion.model.entity;

import co.edu.uniquindio.proyectoprogramacion.model.enumx.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id", nullable = false)
    private Usuario usuarioResponsable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AccionHistorial accion;

    @Column(length = 500)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EstadoSolicitud estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private EstadoSolicitud estadoNuevo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Prioridad prioridadAnterior;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Prioridad prioridadNueva;

    @Column(nullable = false)
    private LocalDateTime fechaHora;
}