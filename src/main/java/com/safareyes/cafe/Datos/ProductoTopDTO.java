package com.safareyes.cafe.Datos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoTopDTO {

    private Integer productoId;
    private String nombre;
    private Long totalVendido;
}
