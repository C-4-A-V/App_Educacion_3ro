package com.example.mpazpro.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearActividadRequest(
    @NotBlank @Pattern(regexp = "lenguaje|matematica|ciencias|historia")
    String asignaturaId,

    @NotBlank @Size(max = 120)
    String titulo,

    @NotBlank @Size(max = 255)
    String pregunta
) {}
