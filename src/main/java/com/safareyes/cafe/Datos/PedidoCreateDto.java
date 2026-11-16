package com.safareyes.cafe.Datos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCreateDto {



    @NotEmpty(message = "El pedido debe tener al menos una línea")
    @Size(max = 15, message = "Un pedido admite como máximo 15 líneas")
    @Valid
    private List<Linea> lineas;

    private String codigoCupon;   // opcional
    private String dniSocio;      // opcional

    // ---------- SALIDA (lo calcula el servidor) ----------

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer turno;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String estado;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaHora;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal subtotal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal descuento;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal total;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal baseImponible;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal cuotaIva;

    // ---------- LÍNEA (clase anidada) ----------

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Linea {

        @NotNull(message = "El producto es obligatorio")
        private Integer productoId;

        @NotNull
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 20, message = "La cantidad máxima es 20")
        private Integer cantidad;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String productoNombre;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal precioUnitario;
    }

}