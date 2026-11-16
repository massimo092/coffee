package com.safareyes.cafe.Datos;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearCuponDTO {


    private Integer id;

    private String codigo;

    private String tipo;

    private BigDecimal valor;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private Integer usosMaximos;

    private Integer usosActuales;

    private BigDecimal importeMinimo;

    private String emailCliente;

    private Boolean activo;
}
