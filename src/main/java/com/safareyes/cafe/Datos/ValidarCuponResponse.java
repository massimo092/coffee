package com.safareyes.cafe.Datos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidarCuponResponse {

    private Boolean valido;
    private BigDecimal descuento;
    private String mensaje;
}
