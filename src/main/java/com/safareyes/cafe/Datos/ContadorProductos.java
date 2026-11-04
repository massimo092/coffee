package com.safareyes.cafe.Datos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContadorProductos {

    private Integer id;
    private String categoria;
    private Long productos;
}
