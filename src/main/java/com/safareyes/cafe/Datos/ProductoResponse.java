package com.safareyes.cafe.Datos;

import java.math.BigDecimal;
import java.util.List;

public record ProductoResponse(
        Integer id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Boolean disponible,
        String categoria,
        List<String> alergenos) {
}