package com.safareyes.cafe.Datos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoRequest {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
