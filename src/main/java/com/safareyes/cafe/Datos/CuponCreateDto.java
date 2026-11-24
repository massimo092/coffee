package com.safareyes.cafe.Datos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CuponCreateDto {

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
