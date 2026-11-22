package com.example.mpazpro.backend;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EstadoRespuestaAulaRequest(
    @NotNull @Min(1) Long actividadId,
    @NotNull @Min(1) Long cursoId,
    @NotBlank String estudianteToken
) {}
