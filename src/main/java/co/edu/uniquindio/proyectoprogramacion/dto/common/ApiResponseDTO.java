package co.edu.uniquindio.proyectoprogramacion.dto.common;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDTO<T> {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private T data;
}