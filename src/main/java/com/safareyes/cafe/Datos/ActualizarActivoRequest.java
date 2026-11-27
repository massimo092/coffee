package com.safareyes.cafe.Datos;

import jakarta.validation.constraints.NotNull;

public record ActualizarActivoRequest(@NotNull Boolean activo) {
}