package com.safareyes.cafe.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Entity
@Table(name = "pedido", schema = "cafeteria")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "numero_turno")
    private Integer numeroTurno;

    @Column(name = "estado")
    private String estado;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "descuento")
    private BigDecimal descuento;

    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "base_imponible")
    private BigDecimal baseImponible;

    @Column(name = "iva")
    private BigDecimal iva;



}
