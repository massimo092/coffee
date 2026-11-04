package com.safareyes.cafe.Datos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoRequest {

    @NotEmpty(message = "El pedido debe tener al menos 1 línea")
    @Size(max = 15, message = "El pedido puede tener como máximo 15 líneas")
    @Valid
    private List<LineaPedidoRequest> lineas;


    private String codigoCupon;


    private String dniSocio;
}
