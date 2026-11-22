package com.example.mpazpro.backend;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RespuestaAulaRequest(
    @NotNull @Min(1) Long actividadId,
    @NotNull @Min(1) Long cursoId,
    @NotBlank String estudianteToken,
    @NotNull @Min(1) @Max(3) Integer numero,
    @NotNull @Min(0) @Max(3) Integer respuestaElegida
) {}
