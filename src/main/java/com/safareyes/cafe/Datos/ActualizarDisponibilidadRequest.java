package com.safareyes.cafe.Datos;

import jakarta.validation.constraints.NotNull;

public record ActualizarDisponibilidadRequest(@NotNull Boolean disponible) {
}