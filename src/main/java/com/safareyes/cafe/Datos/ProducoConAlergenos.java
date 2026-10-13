package com.safareyes.cafe.Datos;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProducoConAlergenos {

    private String producto;

    private String alergenos;
}
