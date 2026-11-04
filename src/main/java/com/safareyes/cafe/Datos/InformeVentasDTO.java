package com.safareyes.cafe.Datos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformeVentasDTO {

    private Long numeroPedidos;
    private BigDecimal total;
    private BigDecimal baseImponible;
    private BigDecimal iva;
}
