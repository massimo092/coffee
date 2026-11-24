package com.safareyes.cafe.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidarCuponResponse {
    private String codigo;
    private BigDecimal descuento;
    private BigDecimal totalConDescuento;
}