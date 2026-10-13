package com.safareyes.cafe.Datos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class lineaPedidoDTO {

    private Long pedidoId;
    private Long numeroTurno;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private Long lineaId;
    private Long cantidad;
    private BigDecimal precio_unitario;
    private Long productoId;
}
