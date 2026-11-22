package com.example.mpazpro.backend;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActividadCompletaRequest(
    @NotNull @Min(1) Long cursoId,
    @NotBlank @Pattern(regexp = "lenguaje|matematica|ciencias|historia") String asignaturaId,
    @NotBlank @Size(min = 3, max = 80) String titulo,
    @NotBlank @Size(max = 1000) String instrucciones,
    @NotBlank @Size(min = 5, max = 500) String pregunta,
    @NotBlank @Size(max = 150) String alternativaA,
    @NotBlank @Size(max = 150) String alternativaB,
    @NotBlank @Size(max = 150) String alternativaC,
    @NotBlank @Size(max = 150) String alternativaD,
    @NotNull @Min(0) @Max(3) Integer respuestaCorrecta,
    @NotNull @Min(1) @Max(3) Integer maxIntentos,
    @NotBlank @Size(max = 1000) String explicacion,
    @NotNull @Size(max = 500) String pista
) {
    @AssertTrue(message = "La actividad debe permitir 1 o 3 intentos")
    public boolean isModalidadValida() {
        return maxIntentos != null && (maxIntentos == 1 || maxIntentos == 3);
    }

    @AssertTrue(message = "Las alternativas deben ser diferentes")
    public boolean isAlternativasDistintas() {
        if (alternativaA == null || alternativaB == null || alternativaC == null || alternativaD == null) return true;
        return java.util.stream.Stream.of(alternativaA, alternativaB, alternativaC, alternativaD)
            .map(valor -> valor.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT))
            .distinct().count() == 4;
    }
}
