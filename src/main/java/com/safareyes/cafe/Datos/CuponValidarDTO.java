package com.safareyes.cafe.Datos;

import com.safareyes.cafe.validacion.CuponValido;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@CuponValido
public class CuponValidarDTO {

    @NotBlank
    private String codigo;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal subtotal;

    private String emailCliente;   // opcional, lo usa el barista
}